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
package org.magnum.dataup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
	
	private Map<Long, Video> videos = new HashMap<Long, Video>();
    private static final AtomicLong currentId = new AtomicLong(0L);

	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideos() {
		return videos.values();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		checkAndSetId(video);
		video.setDataUrl(getDataUrl(video.getId()));
		videos.put(video.getId(), video);
		return video;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void getVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, HttpServletResponse response){
		Video v = videos.get(id);
		try {
			if (v != null && VideoFileManager.get().hasVideoData(v)){
				VideoFileManager.get().copyVideoData(v, response.getOutputStream());
			}
			else{
				throw new ResourceNotFoundException();
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(404);
		}
		
	}
	
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus addVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile data, HttpServletResponse response) {
		Video v = videos.get(id);
		if (v != null){
			try {
				VideoFileManager.get().saveVideoData(v, data.getInputStream());
				return new VideoStatus(VideoState.READY);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		else{
			throw new ResourceNotFoundException();
		}
		
	}
	
	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	
	 private String getDataUrl(long videoId){
         String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
         return url;
     }

  	private String getUrlBaseForLocalServer() {
		   HttpServletRequest request = 
		       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		   String base = 
		      "http://"+request.getServerName() 
		      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		   return base;
		}
	
}
