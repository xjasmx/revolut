import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class TransactionTest {

    private Account acc_A, acc_B, acc_C;

    @Before
    public void setUp() throws Exception {
        acc_A = new Account(new BigDecimal("9.0"));
        acc_B = new Account(new BigDecimal("0.0"));
        acc_C = new Account(new BigDecimal("587.89"));
    }

    @Test
    public void execute_valid() throws Exception {
        Transaction trans_A = new Transaction(acc_A.getId(), acc_B.getId(), new BigDecimal("8.9"));
        trans_A.execute();
        Assert.assertEquals(new BigDecimal("0.1"), acc_A.getBalance());
        Assert.assertTrue(acc_A.getTransactions().contains(trans_A));
        Assert.assertEquals(new BigDecimal("8.9"), acc_B.getBalance());
        Assert.assertTrue(acc_B.getTransactions().contains(trans_A));

        Transaction trans_B = new Transaction(acc_B.getId(), acc_C.getId(), new BigDecimal("8.9"));
        trans_B.execute();
        Assert.assertEquals(new BigDecimal("0.0"), acc_B.getBalance());
        Assert.assertTrue(acc_B.getTransactions().contains(trans_B));
        Assert.assertEquals(new BigDecimal("596.79"), acc_C.getBalance());
        Assert.assertTrue(acc_C.getTransactions().contains(trans_B));

        Transaction trans_C = new Transaction(acc_A.getId(), acc_C.getId(), new BigDecimal("0.1"));
        trans_C.execute();
        Assert.assertEquals(new BigDecimal("0.0"), acc_A.getBalance());
        Assert.assertTrue(acc_A.getTransactions().contains(trans_C));
        Assert.assertEquals(new BigDecimal("596.89"), acc_C.getBalance());
        Assert.assertTrue(acc_C.getTransactions().contains(trans_C));

        Assert.assertTrue(DB.checkIfTransactionExists(trans_A.getId()));
        Assert.assertTrue(DB.checkIfTransactionExists(trans_B.getId()));
        Assert.assertTrue(DB.checkIfTransactionExists(trans_C.getId()));
    }


    @Test
    public void execute_invalid() throws Exception {
        Exception exp = new Exception();
        Transaction trans_A = new Transaction(acc_A.getId(), acc_B.getId(), new BigDecimal("9.1"));
        try {
            trans_A.execute();
        }
        catch (Exception e) {
            exp = e;
        }
        Assert.assertEquals("Insufficient funds", exp.getMessage());
        Assert.assertEquals(new BigDecimal("9.0"), acc_A.getBalance());
        Assert.assertEquals(new BigDecimal("0.0"), acc_B.getBalance());
        Assert.assertFalse(acc_A.getTransactions().contains(trans_A));
        Assert.assertFalse(acc_B.getTransactions().contains(trans_A));
        Assert.assertFalse(DB.getTransaction(trans_A.getId()).getSuccessful());
    }


    @Test
    public void toJson() throws Exception {
        Transaction trans_A = new Transaction(acc_A.getId(), acc_B.getId(), new BigDecimal("9.1"));
        String expectedJson = "{\r\n" +
                "  \"id\" : " + trans_A.getId() + ",\r\n" +
                "  \"sourceAccId\" : " + acc_A.getId() + ",\r\n" +
                "  \"destAccId\" : " + acc_B.getId() + ",\r\n" +
                "  \"amount\" : " +trans_A.getAmount() + ",\r\n" +
                "  \"successful\" : " + trans_A.getSuccessful() + "\r\n" +
                "}";
        Assert.assertEquals(expectedJson.replace("\n", "").replace("\r", ""),
                trans_A.toJson().replace("\n", "").replace("\r", ""));
    }

    @Test
    public void fromJson() throws Exception {
        String jsonTrans = "{\r\n" +
                "  \"id\" : 974217,\r\n" +
                "  \"sourceAccId\" : 456,\r\n" +
                "  \"destAccId\" : 789,\r\n" +
                "  \"amount\" : 89.65,\r\n" +
                "  \"successful\" : true\r\n" +
                "}";
        Transaction trans_A = Transaction.fromJson(jsonTrans);

        Assert.assertEquals(974217, trans_A.getId());
        Assert.assertEquals(456, trans_A.getSourceAccId());
        Assert.assertEquals(789, trans_A.getDestAccId());
        Assert.assertEquals(new BigDecimal("89.65"), trans_A.getAmount());
        Assert.assertEquals(true, trans_A.getSuccessful());
    }
}