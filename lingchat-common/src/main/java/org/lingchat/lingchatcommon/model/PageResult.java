package org.lingchat.lingchatcommon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;

    private long total;

    private Integer pageNum;

    private Integer pageSize;

    private Integer totalPage;
}
