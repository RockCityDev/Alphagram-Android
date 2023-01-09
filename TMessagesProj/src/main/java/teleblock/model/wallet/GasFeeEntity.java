package teleblock.model.wallet;

import java.io.Serializable;

/**
 * Time:2022/9/16
 * Author:Perry
 * Description：polygon gas费用数据
 */
public class GasFeeEntity {

    private String status;
    private String message;
    private GasFeeEntityResult result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GasFeeEntityResult getResult() {
        return result;
    }

    public void setResult(GasFeeEntityResult result) {
        this.result = result;
    }

    public static class GasFeeEntityResult implements Serializable {
        private String LastBlock;
        private String SafeGasPrice;
        private String ProposeGasPrice;
        private String FastGasPrice;
        private String suggestBaseFee;
        private String gasUsedRatio;
        private String UsdPrice;

        public String getLastBlock() {
            return LastBlock;
        }

        public void setLastBlock(String lastBlock) {
            LastBlock = lastBlock;
        }

        public String getSafeGasPrice() {
            return SafeGasPrice;
        }

        public void setSafeGasPrice(String safeGasPrice) {
            SafeGasPrice = safeGasPrice;
        }

        public String getProposeGasPrice() {
            return ProposeGasPrice;
        }

        public void setProposeGasPrice(String proposeGasPrice) {
            ProposeGasPrice = proposeGasPrice;
        }

        public String getFastGasPrice() {
            return FastGasPrice;
        }

        public void setFastGasPrice(String fastGasPrice) {
            FastGasPrice = fastGasPrice;
        }

        public String getSuggestBaseFee() {
            return suggestBaseFee;
        }

        public void setSuggestBaseFee(String suggestBaseFee) {
            this.suggestBaseFee = suggestBaseFee;
        }

        public String getGasUsedRatio() {
            return gasUsedRatio;
        }

        public void setGasUsedRatio(String gasUsedRatio) {
            this.gasUsedRatio = gasUsedRatio;
        }

        public String getUsdPrice() {
            return UsdPrice;
        }

        public void setUsdPrice(String usdPrice) {
            UsdPrice = usdPrice;
        }
    }
}
