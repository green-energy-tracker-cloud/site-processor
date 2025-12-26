package com.green.energy.tracker.cloud.site_processor.model;


import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "sites")
public class SiteWriteDocument {

    @DocumentId
    private String id;
    private String name;
    private String userId;
    private String address;
    private GeoLocationWrite location;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
