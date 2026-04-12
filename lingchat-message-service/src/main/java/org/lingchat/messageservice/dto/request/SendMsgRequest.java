package org.lingchat.messageservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMsgRequest {

    /**
     * 接收者用户ID
     */
    private Long to;

    /**
     * 消息类型：1=文本, 2=图片, 3=文件, 4=语音, 5=视频
     */
    private Integer msgType;

    /**
     * 消息内容
     */
    private String content;
}
