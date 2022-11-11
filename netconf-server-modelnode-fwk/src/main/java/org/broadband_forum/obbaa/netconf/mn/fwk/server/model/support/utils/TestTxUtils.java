/*
 * Copyright 2018 Broadband Forum
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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestTxUtils {
    public static <T> T executeWithinTx(PersistenceManagerUtil util, TxTemplate<T> template) throws Exception {
        util.getEntityDataStoreManager().beginTransaction();
        try {
            T result = template.execute();
            util.getEntityDataStoreManager().commitTransaction();
            return result;
        }catch (Exception e){
            util.getEntityDataStoreManager().rollbackTransaction();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static  <T extends ModelNodeDataStoreManager> T getTxDecoratedDSM(final PersistenceManagerUtil persistenceManagerUtil, final
    ModelNodeDataStoreManager
            dataStoreManager) {
        return (T) Proxy.newProxyInstance(ModelNodeDataStoreManager.class.getClassLoader(),
                new Class[]{ModelNodeDataStoreManager.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        boolean txUserNotDefined = true;
                        if (persistenceManagerUtil.getEntityDataStoreManager() != null &&
                            persistenceManagerUtil.getEntityDataStoreManager().getEntityManager() != null &&
                            persistenceManagerUtil.getEntityDataStoreManager().getEntityManager().getTransaction() != null &&
                            persistenceManagerUtil.getEntityDataStoreManager().getEntityManager().getTransaction().isActive()){
                            txUserNotDefined = false;
                        }
                        if (txUserNotDefined){
                            persistenceManagerUtil.getEntityDataStoreManager().beginTransaction();
                        }
                        try {
                            Object invoke = method.invoke(dataStoreManager, args);
                            if (txUserNotDefined){
                                persistenceManagerUtil.getEntityDataStoreManager().commitTransaction();
                            }
                            return invoke;
                        } catch (Exception e ) {
                            if (txUserNotDefined){
                                persistenceManagerUtil.getEntityDataStoreManager().rollbackTransaction();
                            }
                            throw e;
                        }
                    }
                });
    }
}
