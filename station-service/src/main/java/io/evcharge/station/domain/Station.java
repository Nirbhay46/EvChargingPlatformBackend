package io.evcharge.station.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Audited
@Entity
@Table(name = "stations",
    indexes = {@Index(name = "idx_stations_city", columnList = "city"),
               @Index(name = "idx_stations_status", columnList = "status")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Station {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false) private Double latitude;
    @Column(nullable = false) private Double longitude;

    /** Total chargers / slots available at the station. */
    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    /** Charging power in kW. */
    @Column(name = "power_kw", nullable = false)
    private Integer powerKw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConnectorType connectorType;

    /** Price per kWh in the smallest currency unit (e.g. cents). */
    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 4)
    private BigDecimal pricePerKwh;

    @Column(nullable = false, length = 8)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private StationStatus status = StationStatus.ACTIVE;

    @Column(name = "operator_id")
    private Long operatorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
