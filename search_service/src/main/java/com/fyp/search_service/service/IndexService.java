package com.fyp.search_service.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.fyp.search_service.constant.IndexInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexService {

    ElasticsearchClient elasticsearchClient;
    ResourceLoader resourceLoader;

    public void createIndices(){
        List<IndexInfo> indexInformation = getIndexInformation();
        if(CollectionUtils.isEmpty(indexInformation)){
            return;
        }

        for ( IndexInfo indexInfo : indexInformation){
            delete(indexInfo);
            create(indexInfo);
        }
    }

    private void create(IndexInfo indexInfo) {

        try {
            if(indexInfo.mappingPath() != null){

                elasticsearchClient.indices().create(c -> c.index(indexInfo.name())
                        .mappings(t -> t.withJson(getMappings(indexInfo.mappingPath()))));
            } else {

                    elasticsearchClient.indices().create(c -> c.index(indexInfo.name()));

            }

        } catch (IOException e) {
            log.error("Error while creating indexInfo {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private  void delete(IndexInfo indexInfo) {
        try {
            BooleanResponse exist = elasticsearchClient.indices().exists(e -> e.index(indexInfo.name()));
            if( !exist.value()){
              return;
            }
            elasticsearchClient.indices().delete(d -> d.index(indexInfo.name()));
            log.info("Deleting index {}", indexInfo.name());
        } catch (IOException e) {
            log.error("Error while deleting indexInfo {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<IndexInfo> getIndexInformation( )  {
      ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
      scanner.addIncludeFilter(new AnnotationTypeFilter(Document.class));

      Set<BeanDefinition> beanDefinitionSet =  scanner.findCandidateComponents("com.fyp.search_service");

      return beanDefinitionSet
              .stream()
              .map(IndexService::getIndexInfo)
              .filter(Objects::nonNull)
              .toList();
    }


    //in this return an IndexInfo record
    // which both indexName + mappingPath
    private static IndexInfo getIndexInfo(BeanDefinition beanDefinition) {
        try {
            Class<?> documentClass = Class.forName(beanDefinition.getBeanClassName());
            return new IndexInfo(getIndexName(documentClass),
                    getIndexMappingPath(documentClass));
        } catch (ClassNotFoundException e) {
            log.error("Error while getting index Name {}", e.getMessage(), e);
            return  null;
        }
    }

    private static String getIndexName(Class<?> documentClass){
        Document annotation = documentClass.getAnnotation(Document.class);
        return annotation.indexName();
    }

    private static String getIndexMappingPath(Class<?> documentClass){
        Mapping annotation = documentClass.getAnnotation(Mapping.class);
        if(annotation == null ){
            return null;
        }
        return annotation.mappingPath();
    }


    private InputStream getMappings(String mappingPath){
        try {
            Resource resource = resourceLoader.getResource("classpath:" + mappingPath);
        return resource.getInputStream();
        } catch (IOException e) {
            log.error("Error while getting resources {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
