package teleblock.model.wallet;

import android.text.TextUtils;

import org.web3j.utils.Convert;

/**
 * 创建日期：2022/5/11
 * 描述：
 */
public class NFTInfo {

    public String asset_name; // 资产名称
    public String nft_name; // nft名称
    public String thumb_url; // 缩略图地址
    public String original_url; // 原始图地址
    public String token_id; // 代币id
    public String contract_address; // 合约地址
    public String symbol; // 代币符号
    public String price; // 价格（单位wei）
    public String token_standard; // 代币标准
    public String blockchain; // 所在区块链

    public String getEthPrice() {
        if (TextUtils.isEmpty(price)) {
            return "";
        }
        return Convert.fromWei(price, Convert.Unit.ETHER).toString();
    }
}