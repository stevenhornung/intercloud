package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LinkCloudStoreJob {

	private static Logger log = LoggerFactory.getLogger(LinkCloudStoreJob.class)

	static triggers = {}

	def jmsService

	def execute(context) {
		//String storeName = context.mergedJobDataMap.get('storeName')
		def future = context.mergedJobDataMap.get('future')

		Account account
		boolean isSuccess
		(account, isSuccess) = future.get()
		log.debug "Cloud store link completed with status: {}", isSuccess

		// Send message that client resources need updating
		jmsService.send(queue:'cloudstore.isUpdated', [acctId: account.id])
	}
}
