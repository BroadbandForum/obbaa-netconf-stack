package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import javax.transaction.Transactional;

public class TxService {

    @Transactional(value=javax.transaction.Transactional.TxType.REQUIRED, rollbackOn={RuntimeException.class,Exception.class})
    public <T> T executeWithTxRequired(TxTemplate<T> template) throws TxException {
        return template.execute();
    }

    @Transactional(value= Transactional.TxType.REQUIRES_NEW, rollbackOn={RuntimeException.class,Exception.class})
    public <T> T executeWithTxRequiresNew(TxTemplate<T> template) throws TxException {
        return template.execute();
    }
    
    @Transactional(value=javax.transaction.Transactional.TxType.MANDATORY, rollbackOn={RuntimeException.class,Exception.class})
    public <T> T executeWithTxMandatory(TxTemplate<T> template) throws TxException {
        return template.execute();
    }
    
    @Transactional(value=javax.transaction.Transactional.TxType.SUPPORTS, rollbackOn={RuntimeException.class,Exception.class})
    public <T> T executeWithTxSupports(TxTemplate<T> template) throws TxException {
        return template.execute();
    }
 
}
