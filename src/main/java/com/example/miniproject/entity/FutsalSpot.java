package com.example.miniproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "futsal_spot")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FutsalSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "business_time", length = 100)
    private String businessTime;

    @Column(name = "homepage_url", length = 512)
    private String homepageUrl;

    @Column(name = "img1_url", length = 512)
    private String img1Url;

    @Column(name = "img2_url", length = 512)
    private String img2Url;

    @Column(name = "img3_url", length = 512)
    private String img3Url;

    private double latitude;
    private double longitude;
}
