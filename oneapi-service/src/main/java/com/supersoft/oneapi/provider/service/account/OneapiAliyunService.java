package com.supersoft.oneapi.provider.service.account;

import com.aliyun.bssopenapi20171214.Client;
import com.aliyun.bssopenapi20171214.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阿里云直接查询余额及账单
 */
@Slf4j
@Service("aliyunService")
public class OneapiAliyunService implements OneapiAccountService {
    private static final String YYYY_MM = "yyyy-MM";
    private final Map<String, Client> clientMap = new ConcurrentHashMap<>();

    @Override
    public Double getCredits(OneapiAccountDO account) {
        Client client = getClient(account);
        if (client == null) {
            return null;
        }
        try {
            // 获取余额
            QueryAccountBalanceResponse response = client.queryAccountBalance();
            if (response == null) {
                return null;
            }
            QueryAccountBalanceResponseBody body = response.getBody();
            if (body == null) {
                return null;
            }
            QueryAccountBalanceResponseBody.QueryAccountBalanceResponseBodyData bodyData = body.getData();
            if (bodyData == null) {
                return null;
            }
            String amount = bodyData.getAvailableAmount();
            if (StringUtils.isBlank(amount)) {
                return null;
            }
            double balance = OneapiCommonUtils.shortDouble(Double.parseDouble(amount));
            account.setBalance(balance);
            
            // 获取本月账单
            QueryBillOverviewRequest request = new QueryBillOverviewRequest();
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM);
            String billMonth = currentDate.format(formatter);
            request.setBillingCycle(billMonth);
            QueryBillOverviewResponse billResponse = client.queryBillOverview(request);
            if (billResponse == null) {
                // 即使获取账单失败，也返回余额
                return balance;
            }
            QueryBillOverviewResponseBody billBody = billResponse.getBody();
            if (billBody == null) {
                // 即使获取账单失败，也返回余额
                return balance;
            }
            QueryBillOverviewResponseBody.QueryBillOverviewResponseBodyData billBodyData = billBody.getData();
            if (billBodyData == null) {
                // 即使获取账单失败，也返回余额
                return balance;
            }
            QueryBillOverviewResponseBody.QueryBillOverviewResponseBodyDataItems items = billBodyData.getItems();
            if (items == null  || CollectionUtils.isEmpty(items.getItem())) {
                // 即使获取账单失败，也返回余额
                return balance;
            }
            List<QueryBillOverviewResponseBody.QueryBillOverviewResponseBodyDataItemsItem> bills = items.getItem();
            double sum = bills.stream()
                    .mapToDouble(QueryBillOverviewResponseBody.QueryBillOverviewResponseBodyDataItemsItem::getCashAmount)
                    .sum();
            account.setCost(OneapiCommonUtils.shortDouble(sum));
            return balance;
        } catch (Exception e) {
            log.error("获取余额失败, ak={}, error={}", account.getAk(), e.getMessage());
        }
        return null;
    }

    @Override
    public void analysis(String errorMsg) {

    }

    private Client getClient(OneapiAccountDO account) {
        return clientMap.computeIfAbsent(account.getAk(), k -> {
            try {
                String ak = account.getAk();
                String sk = account.getSk();
                Config config = new Config().setAccessKeyId(ak).setAccessKeySecret(sk);
                config.regionId = "cn-wulanchabu";
                config.endpoint = "business.aliyuncs.com";
                return new Client(config);
            } catch (Exception e) {
                log.error("创建阿里云管理客户端失败", e);
            }
            return null;
        });
    }

}