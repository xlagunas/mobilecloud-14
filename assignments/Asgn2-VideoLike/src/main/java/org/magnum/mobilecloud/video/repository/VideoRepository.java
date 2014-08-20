package org.magnum.mobilecloud.video.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{
	public List<Video> findByName(String title);
	public List<Video> findByDurationLessThan(long duration);
}
