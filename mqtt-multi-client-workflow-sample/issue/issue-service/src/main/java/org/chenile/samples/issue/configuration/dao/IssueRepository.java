package org.chenile.samples.issue.configuration.dao;

import org.chenile.samples.issue.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  public interface IssueRepository extends JpaRepository<Issue,String> {}
