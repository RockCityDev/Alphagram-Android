package teleblock.blockchain;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Arrays;

public class Web3TransactionUtils {

    public static String encodeTransferData(final String toAddress, final BigInteger sum) {
        Function function = new Function(
                "transfer",  // function we're calling
                Arrays.asList(new Address(toAddress), new Uint256(sum)),  // Parameters to pass as Solidity Types
                Arrays.asList(new org.web3j.abi.TypeReference<Bool>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    public static String encodeBalanceOfData(final String ownerAddress) {
        Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(ownerAddress)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    public static String encodeTokenURIData(BigInteger tokenId) {
        Function function = new Function(
                "tokenURI",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Utf8String>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    public static String encodeUriData(BigInteger tokenId) {
        Function function = new Function(
                "uri",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Utf8String>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    public static String encodeTokensOfOwnerData(final String ownerAddress) {
        Function function = new Function(
                "tokensOfOwner",
                Arrays.asList(new Address(ownerAddress)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }
}
