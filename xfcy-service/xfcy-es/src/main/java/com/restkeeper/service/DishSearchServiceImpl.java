package com.restkeeper.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品检索本地服务：基于 Spring Data Elasticsearch（新版 Elasticsearch Java Client）。
 * 双租户字段 shop_id/store_id 作为 filter 条件，商品码/名称用 wildcard 保持原 SQL like 语义。
 */
@Slf4j
@Service("dishSearchService")
public class DishSearchServiceImpl {

    private static final String INDEX_DISH = "dish";

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    //根据商品码与类型检索
    public SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize) {
        return queryDish("code", code, true, type, pageNum, pageSize);
    }

    //根据商品码检索菜品（不限类型）
    public SearchResult<DishEs> searchDishByCode(String code, int pageNum, int pageSize) {
        return queryDish("code", code, false, 0, pageNum, pageSize);
    }

    //根据商品名称与类型检索
    public SearchResult<DishEs> searchDishByName(String name, int type, int pageNum, int pageSize) {
        return queryDish("dish_name", name, true, type, pageNum, pageSize);
    }

    /**
     * 统一构造菜品索引检索条件并执行分页查询
     *
     * @param keywordField 模糊匹配字段（code / dish_name）
     * @param keyword      检索关键词，两侧加 * 模拟 like '%kw%'
     * @param withType    是否追加类型过滤
     * @param type         1 菜品，2 套餐（withType 为 true 时生效）
     */
    private SearchResult<DishEs> queryDish(String keywordField, String keyword, boolean withType,
                                           int type, int pageNum, int pageSize) {
        String shopId = TenantContext.getShopId();
        String storeId = TenantContext.getStoreId();
        if (!StringUtils.hasText(shopId) || "null".equals(shopId)) {
            throw new BussinessException("商户号不存在");
        }
        if (!StringUtils.hasText(storeId) || "null".equals(storeId)) {
            throw new BussinessException("门店号不存在");
        }

        final String wildcardPattern = "*" + keyword + "*";
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    //租户与逻辑删除条件走 filter，不参与评分
                    b.filter(f -> f.term(t -> t.field("shop_id").value(FieldValue.of(shopId))));
                    b.filter(f -> f.term(t -> t.field("store_id").value(FieldValue.of(storeId))));
                    b.filter(f -> f.term(t -> t.field("is_deleted").value(FieldValue.of(0))));
                    if (withType) {
                        b.filter(f -> f.term(t -> t.field("type").value(FieldValue.of(type))));
                    }
                    if (StringUtils.hasText(keyword)) {
                        b.must(m -> m.wildcard(w -> w.field(keywordField)
                                .wildcard(wildcardPattern).caseInsensitive(true)));
                    }
                    return b;
                }))
                .withSort(Sort.by(Sort.Direction.DESC, "last_update_time"))
                .withPageable(PageRequest.of(Math.max(0, pageNum - 1), pageSize))
                .build();

        try {
            SearchHits<DishEs> searchHits = elasticsearchOperations.search(query, DishEs.class);
            List<DishEs> records = new ArrayList<>();
            searchHits.forEach(hit -> records.add(hit.getContent()));

            SearchResult<DishEs> result = new SearchResult<>();
            result.setRecords(records);
            result.setTotal(searchHits.getTotalHits());
            return result;
        } catch (RuntimeException e) {
            log.error("检索菜品索引失败, index={}, field={}, keyword={}", INDEX_DISH, keywordField, keyword, e);
            throw new BussinessException("菜品检索服务异常");
        }
    }
}
