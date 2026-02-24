package com.gfi.zentrum.adapter.out.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.out.McpVerificationPort;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class McpVerificationAdapter implements McpVerificationPort {

    private static final Logger log = LoggerFactory.getLogger(McpVerificationAdapter.class);
    private static final String TOOL_NAME = "validate_berufe";

    private final McpSyncClient mcpClient;
    private final ObjectMapper objectMapper;

    public McpVerificationAdapter(McpSyncClient mcpClient, ObjectMapper objectMapper) {
        this.mcpClient = mcpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public VerificationResult verify(List<Beruf> berufe) {
        try {
            String berufeJson = objectMapper.writeValueAsString(berufe);
            Map<String, Object> arguments = Map.of("berufe", berufeJson);

            McpSchema.CallToolResult toolResult = mcpClient.callTool(
                    new McpSchema.CallToolRequest(TOOL_NAME, arguments)
            );

            return parseToolResult(toolResult);
        } catch (Exception e) {
            log.warn("MCP verification failed, returning unverified: {}", e.getMessage());
            return null;
        }
    }

    private VerificationResult parseToolResult(McpSchema.CallToolResult toolResult) throws JsonProcessingException {
        if (toolResult.isError() != null && toolResult.isError()) {
            String errorMsg = extractTextContent(toolResult);
            log.warn("MCP tool returned error: {}", errorMsg);
            return new VerificationResult(false,
                    List.of(new VerificationIssue(Severity.ERROR, "mcp", errorMsg)),
                    Instant.now());
        }

        String responseJson = extractTextContent(toolResult);
        Map<String, Object> response = objectMapper.readValue(responseJson, new TypeReference<>() {});

        boolean valid = Boolean.TRUE.equals(response.get("valid"));
        List<VerificationIssue> issues = parseIssues(response.get("issues"));

        return new VerificationResult(valid, issues, Instant.now());
    }

    private String extractTextContent(McpSchema.CallToolResult toolResult) {
        if (toolResult.content() == null || toolResult.content().isEmpty()) {
            return "{}";
        }
        return toolResult.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .findFirst()
                .orElse("{}");
    }

    @SuppressWarnings("unchecked")
    private List<VerificationIssue> parseIssues(Object issuesObj) {
        if (issuesObj == null) {
            return List.of();
        }
        List<Map<String, String>> issuesList = objectMapper.convertValue(issuesObj, new TypeReference<>() {});
        return issuesList.stream()
                .map(m -> new VerificationIssue(
                        parseSeverity(m.get("severity")),
                        m.get("field"),
                        m.get("message")
                ))
                .toList();
    }

    private Severity parseSeverity(String severity) {
        if (severity == null) {
            return Severity.INFO;
        }
        try {
            return Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Severity.INFO;
        }
    }
}
