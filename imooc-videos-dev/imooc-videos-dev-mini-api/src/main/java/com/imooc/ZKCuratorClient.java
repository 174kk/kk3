package com.imooc;

import com.imooc.cofig.ResourceConfig;
import com.imooc.enums.BGMOperatorTypeEnum;
import com.imooc.service.BgmService;
import com.imooc.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class ZKCuratorClient {

	// zk客户端
	private CuratorFramework client = null;
	final static Logger log = LoggerFactory.getLogger(ZKCuratorClient.class);

	@Autowired
	private BgmService bgmService;

//    public static final String ZOOKEEPER_SERVER = "192.168.158.128:2181";

	@Autowired
	private ResourceConfig resourceConfig;

	public void init() {
		if (client != null) {
			return;
		}

		// 重试策略
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		// 创建zk客户端
		client = CuratorFrameworkFactory.builder().connectString(resourceConfig.getZookeeperServer())
				.sessionTimeoutMs(20000).retryPolicy(retryPolicy).namespace("admin").build();
		// 启动客户端
		client.start();

		try {
//            String testNodeData = new String(client.getData().forPath("/bgm/190806A68533P1Y4"));
//            log.info("测试的节点数据为：{}", testNodeData);
			addChildWatch("/bgm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addChildWatch(String nodePath) throws Exception {

		final PathChildrenCache cache = new PathChildrenCache(client, nodePath, true);
		cache.start();
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
				if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
					log.info("监听到事件 CHILD_ADDED");

					// 1.从数据库查询BGM对象，获取对象路径
					String path = event.getData().getPath();
					String operatorObjStr = new String(event.getData().getData(), "GBK");
					Map<String, String> map = JsonUtils.jsonToPojo(operatorObjStr, Map.class);

					String operatorType = map.get("operType");
					String songPath = map.get("path");


//                    String[] arr = path.split("/");
//                    String bgmId = arr[arr.length - 1];

//                    Bgm bgm = bgmService.queryBgmById(bgmId);
//                    if (bgm == null) {
//                        return;
//                    }

					// 1.1 bgm所在的相对路径
//                    String songPath = bgm.getPath();

					// 2.定义保存到本地的BGM路径
//                    String filePath = "D:\\videos-dev" + songPath;
					String filePath = resourceConfig.getFileSpace() + songPath;

					// 3.定义下载的路径（播放url）
					String[] arrPath = songPath.split("\\\\");
					String finalPath = "";
					// 3.1 处理url的斜杠以及编码
					for (int i = 0; i < arrPath.length; i++) {
						if (StringUtils.isNotBlank(arrPath[i])) {
							finalPath += "/";

							finalPath += URLEncoder.encode(arrPath[i], "UTF-8");
						}
					}
//                    String bgmUrl = "http://10.10.70.70:8080/mvc" + finalPath;
					System.out.println(resourceConfig.toString());
					String bgmUrl = resourceConfig.getBgmServer() + finalPath;
					System.out.println("bgmUrl:" + bgmUrl);

					if (operatorType.equals(BGMOperatorTypeEnum.ADD.type)) {
						// 下载bgm到SpringBoot服务器
						URL url = new URL(bgmUrl);
						File file = new File(filePath);
						FileUtils.copyURLToFile(url, file);
						client.delete().forPath(path);
					} else if (operatorType.equals(BGMOperatorTypeEnum.DELETE.type)) {
						File file = new File(filePath);
						// 删除
						FileUtils.forceDelete(file);
						client.delete().forPath(path);
					}
				}
			}
		});
	}
}