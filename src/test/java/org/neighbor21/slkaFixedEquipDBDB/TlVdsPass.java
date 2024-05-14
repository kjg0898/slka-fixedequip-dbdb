package org.neighbor21.slkaFixedEquipDBDB;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@Setter
@ToString
@Table(name = "tl_vds_pass", schema = "srlk")
public class TlVdsPass implements Persistable<TlVdsPassKey> {

    @EmbeddedId
    private TlVdsPassKey tlVdsPassKey;
    @Column(name = "instllc_nm")
    private String instllcNm;

    @Column(name = "vhcl_clsf")
    private String vhclClsf;

    @Column(name = "vhcl_clsfnm")
    private String vhclClsfnm;

    @Column(name = "vhcl_clsfgrp")
    private String vhclClsfgrp;

    @Column(name = "rgsph_id")
    private String rgsphId;

    @Column(name = "rgsph_nm")
    private String rgsphNm;

    @Column(name = "speed")
    private long speed;

    @Column(name = "evnt_cd")
    private String evntCd;

    @Column(name = "evnt_nm")
    private String evntNm;

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public TlVdsPassKey getId() {
        return tlVdsPassKey;
    }
}
