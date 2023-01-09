package teleblock.model.wallet;

import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 创建日期：2022/10/20
 * 描述：
 */
public class TTNft {

    public String id;
    public String contractAddress;
    public String mintedTime;
    public String transactionHash;
    public String value;
    public String totalIssue;
    public String type;

    public static List<NFTInfo> parse(List<TTNft> ttNftList) {
        List<NFTInfo> nftInfoList = new ArrayList<>();
        for (TTNft ttNft : ttNftList) {
            NFTInfo nftInfo = new NFTInfo();
            nftInfo.token_id = Numeric.toBigInt(ttNft.id).toString();
            nftInfo.contract_address = ttNft.contractAddress;
            nftInfo.token_standard = ttNft.type;
            nftInfo.blockchain = "Thundercore";
            nftInfoList.add(nftInfo);
        }
        return nftInfoList;
    }
}