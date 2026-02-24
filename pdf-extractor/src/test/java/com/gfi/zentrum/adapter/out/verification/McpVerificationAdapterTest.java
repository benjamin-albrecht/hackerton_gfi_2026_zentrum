package com.gfi.zentrum.adapter.out.verification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfi.zentrum.domain.model.Beruf;
import com.gfi.zentrum.domain.model.Severity;
import com.gfi.zentrum.domain.model.VerificationResult;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpVerificationAdapterTest {

    @Mock
    private McpSyncClient mcpClient;

    private McpVerificationAdapter adapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adapter = new McpVerificationAdapter(mcpClient, objectMapper);
    }

    @Test
    void verifyReturnsValidResult() {
        List<Beruf> berufe = List.of(new Beruf("Kaufmann", List.of(123), List.of()));
        String responseJson = """
                {"valid": true, "issues": []}
                """;
        McpSchema.CallToolResult toolResult = new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent(responseJson)), false
        );
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(toolResult);

        VerificationResult result = adapter.verify(berufe);

        assertNotNull(result);
        assertTrue(result.valid());
        assertTrue(result.issues().isEmpty());
        assertNotNull(result.verifiedAt());
    }

    @Test
    void verifyReturnsInvalidWithIssues() {
        List<Beruf> berufe = List.of(new Beruf("Unknown Beruf", List.of(999), List.of()));
        String responseJson = """
                {
                  "valid": false,
                  "issues": [
                    {"severity": "ERROR", "field": "berufNr", "message": "Unknown beruf code 999"},
                    {"severity": "WARNING", "field": "beschreibung", "message": "Name does not match registry"}
                  ]
                }
                """;
        McpSchema.CallToolResult toolResult = new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent(responseJson)), false
        );
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(toolResult);

        VerificationResult result = adapter.verify(berufe);

        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals(2, result.issues().size());
        assertEquals(Severity.ERROR, result.issues().getFirst().severity());
        assertEquals("berufNr", result.issues().getFirst().field());
    }

    @Test
    void verifyReturnsNullWhenMcpClientFails() {
        List<Beruf> berufe = List.of(new Beruf("Test", List.of(1), List.of()));
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        VerificationResult result = adapter.verify(berufe);

        assertNull(result);
    }

    @Test
    void verifyHandlesToolError() {
        List<Beruf> berufe = List.of(new Beruf("Test", List.of(1), List.of()));
        McpSchema.CallToolResult toolResult = new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent("Tool execution failed")), true
        );
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(toolResult);

        VerificationResult result = adapter.verify(berufe);

        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals(1, result.issues().size());
        assertEquals(Severity.ERROR, result.issues().getFirst().severity());
    }
}
