package org.kapott.hbci.GV.generators;


import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.xml.datatype.DatatypeFactory;

import org.kapott.hbci.sepa.PainVersion;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.AccountIdentificationSEPA;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.ActiveOrHistoricCurrencyAndAmountSEPA;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.ActiveOrHistoricCurrencyCodeEUR;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.AmountTypeSEPA;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.BranchAndFinancialInstitutionIdentificationSEPA1;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.BranchAndFinancialInstitutionIdentificationSEPA3;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.CashAccountSEPA1;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.CashAccountSEPA2;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.ChargeBearerTypeSEPACode;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.CreditTransferTransactionInformationSCT;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.CustomerCreditTransferInitiationV03;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.Document;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.FinancialInstitutionIdentificationSEPA1;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.FinancialInstitutionIdentificationSEPA3;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.GroupHeaderSCT;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.ObjectFactory;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PartyIdentificationSEPA1;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PartyIdentificationSEPA2;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PaymentIdentificationSEPA;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PaymentInstructionInformationSCT;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PaymentMethodSCTCode;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.PaymentTypeInformationSCT1;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.RemittanceInformationSEPA1Choice;
import org.kapott.hbci.sepa.jaxb.pain_001_003_03.ServiceLevelSEPA;

/**
 * SEPA-Generator fuer pain.001.003.03.
 */
public class GenUebSEPA00100303 extends AbstractSEPAGenerator
{
    /**
     * @see org.kapott.hbci.GV.generators.AbstractSEPAGenerator#getPainVersion()
     */
    @Override
    public PainVersion getPainVersion()
    {
        return PainVersion.PAIN_001_003_03;
    }

    /**
     * @see org.kapott.hbci.GV.generators.ISEPAGenerator#generate(java.util.Properties, java.io.OutputStream, boolean)
     */
    @Override
    public void generate(Properties sepaParams, OutputStream os, boolean validate) throws Exception
    {
        Integer maxIndex = maxIndex(sepaParams);

        //Formatter um Dates ins gewŁnschte ISODateTime Format zu bringen.
        Date now=new Date();
        SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DatatypeFactory df = DatatypeFactory.newInstance();


        //Document
        Document doc = new Document();


        //Customer Credit Transfer Initiation
        doc.setCstmrCdtTrfInitn(new CustomerCreditTransferInitiationV03());
        doc.getCstmrCdtTrfInitn().setGrpHdr(new GroupHeaderSCT());


        //Group Header
        doc.getCstmrCdtTrfInitn().getGrpHdr().setMsgId(sepaParams.getProperty("sepaid"));
        doc.getCstmrCdtTrfInitn().getGrpHdr().setCreDtTm(df.newXMLGregorianCalendar(sdtf.format(now)));
        doc.getCstmrCdtTrfInitn().getGrpHdr().setNbOfTxs(String.valueOf(maxIndex != null ? maxIndex + 1 : 1));
        doc.getCstmrCdtTrfInitn().getGrpHdr().setInitgPty(new PartyIdentificationSEPA1());
        doc.getCstmrCdtTrfInitn().getGrpHdr().getInitgPty().setNm(sepaParams.getProperty("src.name"));


        //Payment Information
        ArrayList<PaymentInstructionInformationSCT> pmtInfs = (ArrayList<PaymentInstructionInformationSCT>) doc.getCstmrCdtTrfInitn().getPmtInf();
        PaymentInstructionInformationSCT pmtInf = new PaymentInstructionInformationSCT();
        pmtInfs.add(pmtInf);

        pmtInf.setPmtInfId(sepaParams.getProperty("sepaid"));
        pmtInf.setPmtMtd(PaymentMethodSCTCode.TRF);

        pmtInf.setNbOfTxs(String.valueOf(maxIndex != null ? maxIndex + 1 : 1));
        pmtInf.setCtrlSum(sumBtgValue(sepaParams, maxIndex));

        pmtInf.setPmtTpInf(new PaymentTypeInformationSCT1());
        pmtInf.getPmtTpInf().setSvcLvl(new ServiceLevelSEPA());
        pmtInf.getPmtTpInf().getSvcLvl().setCd("SEPA");

        String date = sepaParams.getProperty("date");
        if(date == null) date = "1999-01-01";
        pmtInf.setReqdExctnDt(df.newXMLGregorianCalendar(date));
        pmtInf.setDbtr(new PartyIdentificationSEPA2());
        pmtInf.setDbtrAcct(new CashAccountSEPA1());
        pmtInf.setDbtrAgt(new BranchAndFinancialInstitutionIdentificationSEPA3());


        //Payment Information - Debtor
        pmtInf.getDbtr().setNm(sepaParams.getProperty("src.name"));


        //Payment Information - DebtorAccount
        pmtInf.getDbtrAcct().setId(new AccountIdentificationSEPA());
        pmtInf.getDbtrAcct().getId().setIBAN(sepaParams.getProperty("src.iban"));


        //Payment Information - DebtorAgent
        pmtInf.getDbtrAgt().setFinInstnId(new FinancialInstitutionIdentificationSEPA3());
        pmtInf.getDbtrAgt().getFinInstnId().setBIC(sepaParams.getProperty("src.bic"));


        //Payment Information - ChargeBearer
        pmtInf.setChrgBr(ChargeBearerTypeSEPACode.SLEV);


        //Payment Information - Credit Transfer Transaction Information
        ArrayList<CreditTransferTransactionInformationSCT> cdtTrxTxInfs = (ArrayList<CreditTransferTransactionInformationSCT>) pmtInf.getCdtTrfTxInf();
        if (maxIndex != null)
        {
            for (int tnr = 0; tnr <= maxIndex; tnr++)
            {
                cdtTrxTxInfs.add(createCreditTransferTransactionInformationSCT(sepaParams, tnr));
            }
        }
        else
        {
            cdtTrxTxInfs.add(createCreditTransferTransactionInformationSCT(sepaParams, null));
        }

        ObjectFactory of = new ObjectFactory();
        this.marshal(of.createDocument(doc), os, validate);
    }

    private CreditTransferTransactionInformationSCT createCreditTransferTransactionInformationSCT(Properties sepaParams, Integer index)
    {
        CreditTransferTransactionInformationSCT cdtTrxTxInf = new CreditTransferTransactionInformationSCT();


        //Payment Information - Credit Transfer Transaction Information - Payment Identification
        cdtTrxTxInf.setPmtId(new PaymentIdentificationSEPA());
        cdtTrxTxInf.getPmtId().setEndToEndId(sepaParams.getProperty(insertIndex("endtoendid", index)));


        //Payment Information - Credit Transfer Transaction Information - Creditor
        cdtTrxTxInf.setCdtr(new PartyIdentificationSEPA2());
        cdtTrxTxInf.getCdtr().setNm(sepaParams.getProperty(insertIndex("dst.name", index)));

        //Payment Information - Credit Transfer Transaction Information - Creditor Account
        cdtTrxTxInf.setCdtrAcct(new CashAccountSEPA2());
        cdtTrxTxInf.getCdtrAcct().setId(new AccountIdentificationSEPA());
        cdtTrxTxInf.getCdtrAcct().getId().setIBAN(sepaParams.getProperty(insertIndex("dst.iban", index)));

        //Payment Information - Credit Transfer Transaction Information - Creditor Agent
        cdtTrxTxInf.setCdtrAgt(new BranchAndFinancialInstitutionIdentificationSEPA1());
        cdtTrxTxInf.getCdtrAgt().setFinInstnId(new FinancialInstitutionIdentificationSEPA1());
        cdtTrxTxInf.getCdtrAgt().getFinInstnId().setBIC(sepaParams.getProperty(insertIndex("dst.bic", index)));


        //Payment Information - Credit Transfer Transaction Information - Amount
        cdtTrxTxInf.setAmt(new AmountTypeSEPA());
        cdtTrxTxInf.getAmt().setInstdAmt(new ActiveOrHistoricCurrencyAndAmountSEPA());
        cdtTrxTxInf.getAmt().getInstdAmt().setValue(new BigDecimal(sepaParams.getProperty(insertIndex("btg.value", index))));

        cdtTrxTxInf.getAmt().getInstdAmt().setCcy(ActiveOrHistoricCurrencyCodeEUR.EUR);



        //Payment Information - Credit Transfer Transaction Information - Usage
        cdtTrxTxInf.setRmtInf(new RemittanceInformationSEPA1Choice());
        cdtTrxTxInf.getRmtInf().setUstrd(sepaParams.getProperty(insertIndex("usage", index)));

        return cdtTrxTxInf;
    }

}
