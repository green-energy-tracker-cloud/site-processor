package com.green.energy.tracker.cloud.site_processor.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import com.green.energy.tracker.cloud.site_processor.model.SiteWriteDocument;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends FirestoreReactiveRepository<SiteWriteDocument> { }