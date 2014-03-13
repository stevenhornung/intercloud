package com.intercloud

import grails.plugin.jms.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessageService {

    private static Logger log = LoggerFactory.getLogger(MessageService.class)

	def jmsService
    static transactional = true
    static exposes = ['jms']

    @Queue(name='cloudstore.isUpdated')
    def setIsUpdatedTrue(msg) {
        Account account = Account.get(msg.acctId)

        log.debug "Changing account isUpdated to true"
		account.isUpdated = true
        account.save()

    	return null
    }

    @Queue(name='cloudstore.finishedUpdate')
    def setIsUpdatedFalse(msg) {
        Account account = Account.get(msg.acctId)

        log.debug "Changing account isUpdated to false"
        account.isUpdated = false
        account.save()

        return null
    }
}