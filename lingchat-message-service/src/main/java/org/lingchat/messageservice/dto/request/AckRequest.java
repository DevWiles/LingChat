package org.lingchat.messageservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckRequest {

    /**
     * 消息ID
     */
    private Long msgId;
}
