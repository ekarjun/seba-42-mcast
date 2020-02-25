/*
 * Copyright 2018-present Open Networking Foundation
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
 */
package org.opencord.cordmcast;

import org.onlab.packet.IpAddress;
import org.onlab.packet.VlanId;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.mcast.api.McastRoute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Optional;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


@Component(immediate = true)
public class CordMcastStatisticsManager<list>
        extends AbstractListenerManager<CordMcastStatisticsEvent, CordMcastStatisticsEventListener>
        implements CordMcastStatisticsService {

    private HashMap<IpAddress, CordMcastStatistics> mcastRouteMap;

    private CordMcastStatisticsDelegate statisticsDelegate;

    private final Logger log = getLogger(getClass());

    @Activate
    public void activate() {
        log.info("Activate CordMcastStatisticsManager");
        statisticsDelegate = new InternalCordMcastDelegateForStatistics();
        eventDispatcher.addSink(CordMcastStatisticsEvent.class, listenerRegistry);
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(CordMcastStatisticsEvent.class);
    }

    @Override
    public void clearMcastRouteMap() {
        mcastRouteMap = new HashMap<IpAddress, CordMcastStatistics>();
    }

    @Override
    public Map<IpAddress, CordMcastStatistics> getMcastStats() {
        return this.mcastRouteMap;
    }

    @Override
    public void setMcastStatistics(McastRoute route, VlanId vlan) {

        if (mcastRouteMap.containsKey(route.group())) {

            HashSet<Optional<IpAddress>> sourceAddressSet;
            sourceAddressSet = mcastRouteMap.get(route.group()).getSourceAddress();
            sourceAddressSet.add(route.source());

            mcastRouteMap.get(route.group()).setSourceAddress(sourceAddressSet);
            mcastRouteMap.get(route.group()).setVlanId(vlan);
        } else {

            HashSet<Optional<IpAddress>> sourceAddressSet = new HashSet<Optional<IpAddress>>();
            sourceAddressSet.add(route.source());

            CordMcastStatistics cordMcastStats = new CordMcastStatistics();
            cordMcastStats.setVlanId(vlan);
            cordMcastStats.setSourceAddress(sourceAddressSet);
            mcastRouteMap.put(route.group(), cordMcastStats);
        }
    }

    @Override
    public CordMcastStatisticsDelegate getStatsDelegate() {
        return statisticsDelegate;
    }

    private class InternalCordMcastDelegateForStatistics implements CordMcastStatisticsDelegate {
        @Override
        public void notify(CordMcastStatisticsEvent cordMcastStatisticsEvent) {
            log.debug("multicast source Stats event {} for {}", cordMcastStatisticsEvent.type(),
                    cordMcastStatisticsEvent.subject());
            post(cordMcastStatisticsEvent);
        }
    }
}
