package com.example.distrcache.persistence

import org.springframework.data.repository.CrudRepository


interface JobStatusRepository : CrudRepository<JobStatusEntity, JobStatusEntityId>