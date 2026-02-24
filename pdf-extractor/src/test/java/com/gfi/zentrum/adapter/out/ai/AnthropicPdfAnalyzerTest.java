package com.gfi.zentrum.adapter.out.ai;

import com.gfi.zentrum.domain.model.Beruf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnthropicPdfAnalyzerTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private CallResponseSpec responseSpec;

    @Test
    @SuppressWarnings("unchecked")
    void analyzeReturnsParsedBerufe() {
        List<Beruf> expected = List.of(
                new Beruf("Anlagenmechaniker/-in", List.of(210), List.of())
        );

        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(any(ParameterizedTypeReference.class))).thenReturn(expected);

        AnthropicPdfAnalyzer analyzer = new AnthropicPdfAnalyzer(chatClientBuilder);
        List<Beruf> result = analyzer.analyze("Ausbildungsberuf: Anlagenmechaniker/-in (210)");

        assertEquals(1, result.size());
        assertEquals("Anlagenmechaniker/-in", result.getFirst().beschreibung());
        assertEquals(List.of(210), result.getFirst().berufNr());
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeThrowsOnApiError() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("API error"));

        AnthropicPdfAnalyzer analyzer = new AnthropicPdfAnalyzer(chatClientBuilder);

        assertThrows(RuntimeException.class, () -> analyzer.analyze("some text"));
    }
}
