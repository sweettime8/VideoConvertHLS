/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.video.service;

/**
 *
 * @author ducnh
 */
public interface VideoService {
     void convertVideoHls(String path);
     void compressVideo(String path);
}
