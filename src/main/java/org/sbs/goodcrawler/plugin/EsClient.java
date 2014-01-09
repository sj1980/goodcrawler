/**
 * ##########################  GoodCrawler  ############################
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sbs.goodcrawler.plugin;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
/**
 * @author shenbaise(shenbaise@outlook.com)
 * @date 2013-7-6 es cilent
 */
public class EsClient {
//	http://localhost:9200/_cluster/nodes/ZECZJwWNRRKOhEL6mlJdBg/_shutdown
	private static Log log = LogFactory.getLog(EsClient.class);
	
	static Settings settings = ImmutableSettings.settingsBuilder()
	// .put("cluster.name", "ES-index")
			.put("client.transport.sniff", true).build();

	private static Client client = null;
	
	private static String mapping = "{  \"0\": {    \"_all\": {      \"enabled\": true    },    \"index_analyzer\": \"ik\",    \"search_analyzer\": \"ik\",    \"_timestamp\": {      \"enabled\": true,      \"format\": \"YYYY-MM-dd\"    },    \"dynamic_templates\": [      {        \"string_template\": {          \"match\": \"*\",          \"mapping\": {            \"type\": \"string\",            \"index\": \"not_analyzed\"          },          \"match_mapping_type\": \"string\"        }      }    ],    \"properties\": {      \"title\": {        \"type\": \"string\",        \"include_in_all\": true,        \"index\": \"analyzed\"      },      \"actors\": {        \"type\": \"string\",        \"include_in_all\": true,        \"index\": \"analyzed\"      },      \"director\": {        \"type\": \"string\",        \"include_in_all\": true,        \"index\": \"analyzed\"      },      \"summary\": {        \"type\": \"string\",        \"include_in_all\": false,        \"index\": \"not_analyzed\"      },      \"type\": {        \"type\": \"string\",        \"include_in_all\": true,        \"index\": \"analyzed\"      },      \"category\": {        \"type\": \"string\",        \"include_in_all\": true,        \"index\": \"analyzed\"      }    }  }}";
	private static String esPath = null;
	static {
		String basePath = new File("").getAbsolutePath();
		esPath = basePath + "/" + "elasticsearch/bin";
		
		client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"127.0.0.1", 9300));
	}

	public static synchronized Client getClient() {
		if (null == client) {
			client = new TransportClient(settings)
			.addTransportAddress(new InetSocketTransportAddress(
					"127.0.0.1", 9300));
		}
		return client;
	}
	
	public static void resetClient(){
		client = null;
	}
	public static void index(String index, String type, Map<String, Object> data) {
		try {
			XContentBuilder xBuilder = jsonBuilder().startObject();
			Set<Entry<String, Object>> sets = data.entrySet();
			for(Entry<String, Object> entry:sets){
				xBuilder.field(entry.getKey()).value(entry.getValue());
			}
			xBuilder.endObject();
//			IndexResponse response = 
					client.prepareIndex(index, type)
					.setId((String)data.get("title"))
					.setSource(xBuilder).execute().actionGet();
			// what does respose contains?
		} catch (ElasticSearchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Index
	 * @param index
	 * @param type
	 * @param id
	 * @param json
	 */
	public static void index(String index, String type, String id,String json) {
		try {
			client.prepareIndex(index, type)
			.setId(id)
			.setSource(json).execute().actionGet();
		} catch (ElasticSearchException e) {
			e.printStackTrace();
		} 
	}
	
	public static SearchResponse search(String index,String id){
		SearchResponse response = client.prepareSearch(index)
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(termQuery("_id", id))
		        .setFrom(0).setSize(10).setExplain(false)
		        .execute()
		        .actionGet();
		return response;
	}
	
	public static void distroy() {
		client.close();
	}
	
	/**
	 * mapping
	 * @return
	 */
	public XContentBuilder getMapping(){
		try {
			XContentBuilder mapping = jsonBuilder().startObject()
	        		.startObject("movie")
	        				.startObject("_source")
    						.field("enabled" , "true")
    						.field("compress", true)
//		        						.field("compress_threshold", "200b")
	        				.endObject()
	        				.startObject("_all")
	        						.field("enabled" , "true")
	        				.endObject()
	        				
	        				.startObject("_index")
	        						.field("enabled" , "false")
	        				.endObject()
	        				
	        				.startObject("_type")
	        					.field("index", "yes")
	        					.field("store", "yes")
	        					.field("index", "not_analyzed")
	        				.endObject()
	        				
	        				.startObject("_id")
	        					.field("index", "yes")
	        					.field("store", "yes")
	        					.field("index", "not_analyzed")
	        				.endObject()
	        				
	        				 .startObject("_analyzer").field("path", "field_analyzer").endObject()
	        				
	                        .startObject("properties") 
	                                .startObject("pm") 
	                                        .field("type", "string") 
	                                .endObject() 
	                                
	                                .startObject("nd") 
	                                        .field("type", "integer") 
	                                        .field("omit_norms","yes")
	                                        .field("omit_term_freq_and_positions","yes")
	                                        .field("index", "not_analyzed")
	                                .endObject()
	                                
	                                .startObject("ym") 
	                                        .field("type", "string")
	                                .endObject()
	                                
	                                .startObject("url")
	                                	.field("type","string")
	                                	.field("index", "no")
	                                	.field("store", "yes")
	                                .endObject()
	                                
	                        .endObject() 
	                .endObject()
	                .endObject();
			
			return mapping;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 设置mapping
	 */
	public void putMapping(){
		try {
			client.admin().indices().preparePutMapping("movie")
			.setType("0").setSource(mapping).execute().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建索引并设置mapping
	 * @param index
	 * @param type
	 * @param mapping
	 */
	public static void createIndexAndMapping(String index,String type,String mapping){
			client.prepareIndex(index, type).execute().actionGet();
			client.admin().indices().preparePutMapping("movie")
			.setSource(mapping).setType(type)
			.execute().actionGet();
		
	}
	
	
	/**
	 * es 是否可用
	 * @return
	 */
	public static boolean isEsStarted(){
		ClusterStateRequest csr = new ClusterStateRequest();
		try {
			boolean b = false;
			for(int i=0;i<10;){
				b = false;
				EsClient.getClient().admin().cluster().state(csr).get();
				b = true;
				if(b){
					return b;
				}
				i++;
				Thread.sleep(3000L);
			}
		} catch (Exception e) {
			EsClient.resetClient();
		}
		return false;
	}
	
	/**
	 * start local es node
	 */
	public synchronized static void startES(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				ClusterStateRequest csr = new ClusterStateRequest();
				try {
					EsClient.getClient().admin().cluster().state(csr).get();
				} catch (Exception e) {
					try {
						EsClient.resetClient();
						String cmd = "elasticsearch";
						if(SystemUtils.OS_NAME.toLowerCase().contains("windows")){
							cmd += cmd+".bat";
						}
						Process process = Runtime.getRuntime().exec(esPath+"/elasticsearch.bat");
						System.out.println("hello");
						process.waitFor();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}, "es start").start();
		try {
			log.info("wait es to start up ...");
			Thread.sleep(3000L);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * stop es cluster
	 */
	public synchronized static void stopES(){
		try {
			Map<String, NodeInfo> m = EsClient.getClient().admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet().getNodesMap();
			for(NodeInfo node:m.values()){
				log.info(node.getNode().getId() + " is shutdown !");
				EsClient.getClient().admin().cluster().nodesShutdown(new NodesShutdownRequest(node.getNode().getId())).actionGet();
			}
			EsClient.getClient().admin().cluster().nodesShutdown(new NodesShutdownRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SearchResponse response = EsClient.search("movie", "魔境仙踪[国语高清]");
		System.out.println(response.toString());
//		response.getHits().getTotalHits();
//		createIndexAndMapping("movie", "0", mapping);
		GetResponse get =client.prepareGet("movie", "0","魔境仙踪[国语高清]" )
		.execute()
		.actionGet();
		
		System.out.println(get.toString());
	}

}
