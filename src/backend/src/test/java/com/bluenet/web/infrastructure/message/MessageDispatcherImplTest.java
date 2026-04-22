package com.bluenet.web.infrastructure.message;

import com.bluenet.web.application.message.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageDispatcherImplTest {

    @Test
    void dispatch_withSupportedChannel_shouldRouteToMatchingStrategy() {
        MessageSenderStrategy strategy = mock(MessageSenderStrategy.class);
        when(strategy.channel()).thenReturn(MessageChannel.EMAIL);
        MessageDispatcherImpl dispatcher = new MessageDispatcherImpl(List.of(strategy));
        MessageRequest request = MessageRequest.text(MessageChannel.EMAIL, "user@test.com", "subject", "content");

        dispatcher.dispatch(request);

        verify(strategy).send(request);
    }

    @Test
    void dispatch_withoutSupportedChannel_shouldThrow() {
        MessageDispatcherImpl dispatcher = new MessageDispatcherImpl(List.of());
        MessageRequest request = MessageRequest.text(MessageChannel.EMAIL, "user@test.com", "subject", "content");

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(request));
    }

}
