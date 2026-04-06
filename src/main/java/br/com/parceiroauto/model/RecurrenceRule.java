package br.com.parceiroauto.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "RecurrenceRule")

public class RecurrenceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
}
