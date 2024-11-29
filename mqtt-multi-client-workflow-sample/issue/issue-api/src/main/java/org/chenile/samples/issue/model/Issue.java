package org.chenile.samples.issue.model;

import jakarta.persistence.*;
import org.chenile.jpautils.entity.AbstractJpaStateEntity;

import java.io.Serial;
import java.util.List;

@Entity
@Table(name = "issue")
public class Issue extends AbstractJpaStateEntity {
	public String assignee;
	public String assignComment;
	public String closeComment;
	public String resolveComment;
	public String description;
	public String openedBy;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL )

	public List<Task> tasks;

	@Override
	protected String getPrefix() {
		return "issue";
	}
}
