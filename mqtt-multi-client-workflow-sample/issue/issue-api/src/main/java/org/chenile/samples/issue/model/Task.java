package org.chenile.samples.issue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaEntity;

@Entity
@Table(name = "task")
public class Task extends BaseJpaEntity {
    public static final String OPENED = "OPENED";
    public static final String COMPLETED = "COMPLETED";
    @Override
    protected String getPrefix() {
        return "task";
    }
    public String name;
    public String description;
    public String stateId=OPENED;
    public String completedComment;
}
