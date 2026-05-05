package com.plathanus.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String produto;

    private Integer quantidade;

    private String unidade;
}
