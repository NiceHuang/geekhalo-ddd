package com.geekhalo.ddd.lite.demo.domain.news.info;

import com.geekhalo.ddd.lite.codegen.application.GenApplication;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@GenApplication
public interface NewsInfoRepository extends BaseNewsInfoRepository{

    default Page<NewsInfo> findValidByCategoryId(Long categoryId, Pageable pageable){
        // 查找有效状态
        Predicate valid = QNewsInfo.newsInfo.status.eq(NewsInfoStatus.ENABLE);
        return findByCategoryId(categoryId, valid, pageable);
    }
}
