/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections.IteratorUtils;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VideoController {

	@Autowired
	private VideoRepository videoRepository;

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody List<Video> getVideos() {
		return IteratorUtils.toList(videoRepository.findAll().iterator());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video) {
		video.setLikes(0);
		return videoRepository.save(video);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideo(@PathVariable("id") long id) {
		Video v = videoRepository.findOne(id);
		if (v == null)
			throw new EntityNotFoundException();
		return v;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public ResponseEntity<Void> likeVideo(@PathVariable("id") long id,
			Principal principal) {
		Video v = videoRepository.findOne(id);
		if (v == null)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		
		List<String> users = v.getUsers();
		if (users.contains(principal.getName())){
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		users.add(principal.getName());
		v.setLikes(users.size());
		videoRepository.save(v);
		return new ResponseEntity<Void>(HttpStatus.OK);

	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public ResponseEntity unlikeVideo(@PathVariable("id") long id,
			Principal principal) {
		Video v = videoRepository.findOne(id);
		if (v == null)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		
		List<String> users = v.getUsers();
		if (users.contains(principal.getName())){
			users.remove(principal.getName());
			v.setLikes(users.size());
			videoRepository.save(v);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody List<String> getLikedBy(@PathVariable("id") long id) {
		Video v = videoRepository.findOne(id);
		
		return v.getUsers();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH
			+ "/search/findByName", method = RequestMethod.GET)
	public @ResponseBody List<Video> findByTitle(@RequestParam("title") String title) {
		List<Video> videos = videoRepository.findByName(title);
		return videos;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH
			+ "/search/findByDurationLessThan", method = RequestMethod.GET)
	public @ResponseBody List<Video> findByDuration(@RequestParam("duration") long duration) {
		List<Video> videos =  videoRepository.findByDurationLessThan(duration);
		return videos;
	}

}
