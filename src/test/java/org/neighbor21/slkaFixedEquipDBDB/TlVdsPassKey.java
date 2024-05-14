package org.neighbor21.slkaFixedEquipDBDB;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Embeddable
@NoArgsConstructor
public class TlVdsPassKey implements Serializable {
    @Column(name = "pass_dt")
    private LocalDateTime passDt;
    @Column(name = "camera_id")
    private String cameraId;
    @Column(name = "passvhcl_id")
    private String passvhclId;

    public TlVdsPassKey(LocalDateTime passDt, String cameraId, String passvhclId) {
        this.passDt = passDt;
        this.cameraId = cameraId;
        this.passvhclId = passvhclId;
    }
}
