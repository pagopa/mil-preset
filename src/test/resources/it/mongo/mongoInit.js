db = connect( 'mongodb://localhost/mil' );

db.subscribers.insertMany([
			{ 
				_id: 'XYZ13243XXYYZZ',
				lastUsageTimestamp: '2023-05-08T10:55:57',
				acquirerId: '4585625',
				channel: 'POS',
				merchantId: '4585625',
				terminalId: '0aB9wXyZ',
				paTaxCode: '15376371009',
				subscriberId: 'x46tr3',
				label: 'description',
				subscriptionTimestamp: '2023-05-05T09:31:33',
			},
			{ 
				_id: 'AYZ13243XXYYZA',
				lastUsageTimestamp: '2023-05-08T10:55:57',
				acquirerId: '4585625',
				channel: 'POS',
				merchantId: '4585625',
				terminalId: '0aB9wXyZ',
				paTaxCode: '11111111111',
				subscriberId: 'a25tr0',
				label: 'description',
				subscriptionTimestamp: '2023-05-05T09:31:33',
			}
			
		])
		
db.presets.insertMany([
	{ 
	  "_id": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	  "creationTimestamp": "2023-05-16T11:13:55.532",
	  "noticeNumber": "585564829563528562",
	  "noticeTaxCode": "15376371009",
	  "operationType": "PAYMENT_NOTICE",
	  "paTaxCode": "15376371009",
	  "presetId": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	  "status": "TO_EXECUTE",
	  "statusTimestamp": "2023-05-16T11:13:55.532",
	  "subscriberId": "x46tr3",
	  "statusDetails": {
	    "transactionId": "517a4216840E461fB011036A0fd134E1",
	    "acquirerId": "4585625",
	    "channel": "POS",
	    "merchantId": "28405fHfk73x88D",
	    "terminalId": "0aB9wXyZ",
	    "insertTimestamp": "2023-04-11T16:20:34",
	    "notices": [
	      {
	        "paymentToken": "648fhg36s95jfg7DS",
	        "paTaxCode": "15376371009",
	        "noticeNumber": "485564829563528563",
	        "amount": 12345,
	        "description": "Health ticket for chest x-ray",
	        "company": "ASL Roma",
	        "office": "Ufficio di Roma"
	      }
	    ],
	    "totalAmount": 12395,
	    "fee": 50,
	    "status": "PRE_CLOSE"
	  },
	},
	{ 
	  "_id": "66657c64-0870-407a-b2cb-0f948b04fb8b",
	  "creationTimestamp": "2023-05-16T12:12:52.532",
	  "noticeNumber": "585564829563528562",
	  "noticeTaxCode": "15376371009",
	  "operationType": "PAYMENT_NOTICE",
	  "paTaxCode": "22276371000",
	  "presetId": "77457c64-0870-407a-b2cb-0f948b04fb9a",
	  "status": "TO_EXECUTE",
	  "statusTimestamp": "2023-05-16T12:12:52.532",
	  "subscriberId": "aal0aa",
	  "statusDetails": {
	    "transactionId": "517a4216840E461fB011036A0fd134E1",
	    "acquirerId": "4585625",
	    "channel": "POS",
	    "merchantId": "28405fHfk73x88D",
	    "terminalId": "0aB9wXyZ",
	    "insertTimestamp": "2023-04-11T16:20:34",
	    "notices": [
	      {
	        "paymentToken": "648fhg36s95jfg7DS",
	        "paTaxCode": "15376371009",
	        "noticeNumber": "485564829563528563",
	        "amount": 12345,
	        "description": "Health ticket for chest x-ray",
	        "company": "ASL Roma",
	        "office": "Ufficio di Roma"
	      }
	    ],
	    "totalAmount": 12395,
	    "fee": 50,
	    "status": "PRE_CLOSE"
	  }
	}
	
])
		
		