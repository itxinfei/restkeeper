package com.restkeeper.vo.store;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 柱状图
 */
@Data
public class BarChartCollectVO {
    private List<String> xAxis = new ArrayList<>();
    private List<Integer> series = new ArrayList<>();
}
