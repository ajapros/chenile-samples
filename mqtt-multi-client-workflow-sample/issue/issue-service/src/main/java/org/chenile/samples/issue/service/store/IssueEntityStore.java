package org.chenile.samples.issue.service.store;

import org.chenile.base.exception.NotFoundException;
import org.chenile.samples.issue.configuration.dao.IssueRepository;
import org.chenile.samples.issue.model.Issue;
import org.chenile.utils.entity.service.EntityStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class IssueEntityStore implements EntityStore<Issue>{
	@Autowired private IssueRepository issueRepository;

	@Override
	public void store(Issue entity) {
		issueRepository.save(entity);
	}

	@Override
	public Issue retrieve(String id) {
		Optional<Issue> issue = issueRepository.findById(id);
		if (issue.isPresent()) return issue.get();
		throw new NotFoundException(1500,"Unable to find order with ID " + id);
	}
}
