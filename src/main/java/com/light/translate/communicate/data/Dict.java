package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_dict")
public class Dict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_type", nullable = false, length = 50)
    private String dictType;

    @Column(name = "dict_key", nullable = false, length = 50)
    private String dictKey;

    @Column(name = "dict_value", nullable = false, length = 100)
    private String dictValue;

    @Column(name = "sort", columnDefinition = "int default 0")
    private Integer sort;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime = new Date();
}
