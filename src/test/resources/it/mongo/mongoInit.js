db = connect( 'mongodb://localhost/mil' );

db.subscribers.insertMany([
			{ 
				"_id": "XYZ13243XXYYZZ",
				"subscriber": {
				    "acquirerId": "4585625",
				    "channel": "POS",
				    "label": "Reception POS",
				    "lastUsageTimestamp": "2023-05-17T14:50:05.446",
				    "merchantId": "23533",
				    "paTaxCode": "15376371009",
				    "subscriberId": "x46tr3",
				    "subscriptionTimestamp": "2023-05-17T14:50:05.446",
				    "terminalId": "0aB9wXyZ"
				  }
			},
			{ 
				"_id": "AYZ13243XXYYZA",
				"subscriber": {
				    "acquirerId": "4585625",
				    "channel": "POS",
				    "label": "Reception POS",
				    "lastUsageTimestamp": "2023-05-17T14:50:05.446",
				    "merchantId": "23533",
				    "paTaxCode": "11111111111",
				    "subscriberId": "x46tr3",
				    "subscriptionTimestamp": "2023-05-17T14:50:05.446",
				    "terminalId": "a25tr0"
				  }
			}
			
		]);
		
printjson( db.subscribers.find( {} ) );

db.presets.insertMany([
	{
	  "_id": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	  "presetOperation": {
	    "creationTimestamp": "2023-05-16T11:13:55.532",
	    "noticeNumber": "585564829563528562",
	    "noticeTaxCode": "15376371009",
	    "operationType": "PAYMENT_NOTICE",
	    "paTaxCode": "15376371009",
	    "presetId": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	    "status": "TO_EXECUTE",
	    "statusDetails": {
	      "acquirerId": "4585625",
	      "channel": "POS",
	      "fee": 50,
	      "insertTimestamp": "2023-05-29T14:21:11",
	      "merchantId": "23533",
	      "notices": [
	        {
	          "amount": 1020000,
	          "company": "my company",
	          "description": "my description",
	          "noticeNumber": "485564829563528563",
	          "office": "Rome office",
	          "paTaxCode": "15376371009",
	          "paymentToken": "t092894109jdkshd"
	        }
	      ],
	      "status": "PRE_CLOSE",
	      "terminalId": "0aB9wXyZ",
	      "totalAmount": 1020000,
	      "transactionId": "517a4216840E461fB011036A0fd134E1"
	    },
	    "statusTimestamp": "2023-05-29T15:00:47",
	    "subscriberId": "x46tr3"
	  }
	},
	{
	  "_id": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	  "presetOperation": {
	    "creationTimestamp": "2023-05-16T11:13:55.532",
	    "noticeNumber": "585564829563528562",
	    "noticeTaxCode": "25376371000",
	    "operationType": "PAYMENT_NOTICE",
	    "paTaxCode": "15376371009",
	    "presetId": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	    "status": "TO_EXECUTE",
	    "statusDetails": {
	      "acquirerId": "4585625",
	      "channel": "POS",
	      "fee": 50,
	      "insertTimestamp": "2023-05-29T14:21:11",
	      "merchantId": "23533",
	      "notices": [
	        {
	          "amount": 1020000,
	          "company": "my company",
	          "description": "?",
	          "noticeNumber": "485564829563528563",
	          "office": "Italy office",
	          "paTaxCode": "25376371000",
	          "paymentToken": "t092894109jdkshd"
	        }
	      ],
	      "status": "PRE_CLOSE",
	      "terminalId": "0aB9wXyZ",
	      "totalAmount": 1020000,
	      "transactionId": "517a4216840E461fB011036A0fd134E1"
	    },
	    "statusTimestamp": "2023-05-29T15:00:47",
	    "subscriberId": "csl0kq"
	  }
	}
	
]);
		
printjson( db.subscribers.find( {} ) );
		