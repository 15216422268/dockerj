package com.example.dockertest.ctrl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * @Author: LiHaohao
 * @program: docker-test
 * @create: 2022-02-16 13:59
 * @Description:
 */
@Slf4j
@Data
@RestController
@RequestMapping("docker")
public class DockerJ {

    @Value("${docker.host:/var/run/docker.sock}")
    private String dockerHost;

    @Value("${api.version:1.41}")
    private String apiVersion;

    private DockerClient dockerClient;

    @PostConstruct
    private void init() {
        dockerHost = "unix://" + dockerHost;
        log.info("dockerHost: {}, apiVersion: {}", dockerHost, apiVersion);
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost).withDockerTlsVerify(false)
                    // .withDockerHost("unix:///Users/yfmeng/.docker/run/docker.sock").withDockerTlsVerify(false)
                    .withApiVersion(apiVersion).build();
            DockerHttpClient httpClient  = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig())
                    .maxConnections(10).build();
            dockerClient = DockerClientImpl.getInstance(config, httpClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // test();
    }

    public void test() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        log.info("images: {}", images);

        List<Container> containers = dockerClient.listContainersCmd().exec();
        log.info("containers: {}", containers);
    }

    @GetMapping("stop")
    @ResponseBody
    public String stop(String id) {
        log.info("id: {}", id);
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(Collections.singletonList("shenzhen_p2")).exec();
        log.info("containers: {}", containers);
        if (containers != null && containers.size() > 0) {
            id = containers.get(0).getId();
            log.info("new id: {}", id);
        }
        InspectContainerResponse dockerInfo = dockerClient.inspectContainerCmd(id).exec();
        log.info("dockerInfo: {}", dockerInfo);
        // Void stop = dockerClient.stopContainerCmd(id).exec();
        // log.info("result: {}", stop);
        return "success";
    }

}
