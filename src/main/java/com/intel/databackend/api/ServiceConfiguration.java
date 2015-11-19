/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.databackend.api;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import com.intel.databackend.api.inquiry.advanced.AdvancedDataInquiryService;
import com.intel.databackend.api.inquiry.basic.BasicDataInquiryService;
import com.intel.databackend.config.DashboardCredentialsProvider;
import com.intel.databackend.config.cloudfoundry.DashboardEndpointConfig;
import com.intel.databackend.datasources.dashboard.auth.AuthApi;
import com.intel.databackend.datasources.dashboard.auth.AuthRestApi;
import com.intel.databackend.datasources.dashboard.components.ComponentsDao;
import com.intel.databackend.datasources.dashboard.components.ComponentsRestApi;
import com.intel.databackend.datasources.dashboard.devices.DeviceDao;
import com.intel.databackend.datasources.dashboard.devices.DeviceRestApi;
import com.intel.databackend.datasources.hbase.DataDao;
import com.intel.databackend.datasources.hbase.DataHbaseDao;
import com.intel.databackend.exceptions.VcapEnvironmentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class ServiceConfiguration {

    @Value("${vcap.application.name:local}")
    private String hbasePrefix;

    @Bean
    public DashboardCredentialsProvider dashboardCredentialsProvider() {
        return new DashboardEndpointConfig();
    }

    @Bean
    public DeviceDao deviceDao(DashboardCredentialsProvider dashboardCredentialsProvider, AuthApi authApi) throws VcapEnvironmentException {
        return new DeviceRestApi(dashboardCredentialsProvider, authApi);
    }

    @Bean
    public ComponentsDao componentsDao(DashboardCredentialsProvider dashboardCredentialsProvider, AuthApi authApi) throws VcapEnvironmentException {
        return new ComponentsRestApi(dashboardCredentialsProvider, authApi);
    }

    @Bean
    public DataDao dataDao() {
        return new DataHbaseDao(hbasePrefix);
    }

    @Bean
    public AuthApi authApi(DashboardCredentialsProvider dashboardCredentialsProvider) throws VcapEnvironmentException {
        return new AuthRestApi(dashboardCredentialsProvider);
    }

    @Bean
    @Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public Service advancedDataInquiryService(DataDao dataDao, DeviceDao deviceDao, ComponentsDao componentsDao) {
        return new AdvancedDataInquiryService(dataDao, componentsDao, deviceDao);
    }

    @Bean
    @Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public Service basicDataInquiryService(DataDao dataDao, ComponentsDao componentsDao) {
        return new BasicDataInquiryService(dataDao, componentsDao);
    }

    @Bean
    @Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public Service firstLastTimestampService(DataDao dataDao, ComponentsDao componentsDao) {
        return new FirstLastTimestampService(dataDao, componentsDao);
    }

    @Bean
    @Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public Service dataSubmissionService(DataDao dataDao) {
        return new DataSubmissionService(dataDao);
    }

}
