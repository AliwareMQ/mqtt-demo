//
//  ViewController.swift
//  MQTTSwift
//
//  Created by Christoph Krey on 23.05.16.
//  Copyright © 2016 OwnTracks. All rights reserved.
//

import UIKit
import MQTTClient

class ViewController: UIViewController, MQTTSessionDelegate {
    private let session = MQTTSession()!
    private var subscribed = false
    @IBOutlet weak var status: UILabel!
    @IBOutlet weak var messages: UITextView!
    @IBOutlet weak var subscriptionStatus: UILabel!
    @IBOutlet weak var publishText: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // 普通无认证
        // 设置下面的参数后，修改代码中的topic名称（代码中为test，需要在控制台提前创建）
        session.transport = MQTTCFSocketTransport()
        session.transport.host = "xxxxxx.mqtt.aliyuncs.com"
        session.transport.port = 1883

//         // 单向认证
//         session.transport = MQTTCFSocketTransport()
//         session.transport.host = "xxxxxx.mqtt.aliyuncs.com"
//         session.transport.port = 8883
//         session.transport.tls = true

        // 双向认证
//         let transport = MQTTSSLSecurityPolicyTransport()
//         // 设置下面的参数后，修改代码中的topic名称（代码中为test，需要在控制台提前创建）
//         transport.host = "mqtt-cn-xxxxxx.mqtt.aliyuncs.com"
//         transport.port = 8883
//         transport.tls = true;
//         transport.certificates = xxx // 客户端证书链
//
//         let securityPolicy = MQTTSSLSecurityPolicy();
//         securityPolicy.allowInvalidCertificates = true;
//         securityPolicy.validatesDomainName = false;
//         securityPolicy.validatesCertificateChain = false;
//         securityPolicy.pinnedCertificates = xxxx // CA证书
//         transport.securityPolicy = securityPolicy;
//
//         session.transport = transport


        // 设备ID，必须以GroupId@@@clientId的形式
        session.clientId = "GID_test@@@test"
        // 以签名鉴权的方式举例，生成签名鉴权的用户名密码
        session.userName = "Signature|xxxxx|instanceId"
        session.password = "xxxx"
        session.delegate = self
    }
    
    func handleEvent(_ session: MQTTSession!, event eventCode: MQTTSessionEvent, error: Error!) {
        switch eventCode {
        case .connected:
            self.status.text = "Connected"
        case .connectionClosed:
            self.status.text = "Closed"
        case .connectionClosedByBroker:
            self.status.text = "Closed by Broker"
        case .connectionError:
            self.status.text = "Error"
        case .connectionRefused:
            self.status.text = "Refused"
        case .protocolError:
            self.status.text = "Protocol Error"
        }
    }
    
    func newMessage(_ session: MQTTSession!, data: Data!, onTopic topic: String!, qos: MQTTQosLevel, retained: Bool, mid: UInt32) {
        var text = self.messages.text ?? ""
        text.append("\n topic - \(topic!) data - \(data!)")
        self.messages.text = text
    }
    
    func subAckReceived(_ session: MQTTSession!, msgID: UInt16, grantedQoss qoss: [NSNumber]!) {
        self.subscribed = true
        self.subscriptionStatus.text = "Subscribed"
    }
    
    func unsubAckReceived(_ session: MQTTSession!, msgID: UInt16) {
        self.subscribed = false
        self.subscriptionStatus.text = "Unsubscribed"
    }
    
    @IBAction func subscribeUnsubscribe(_ sender: Any) {
        if self.subscribed {
            session.unsubscribeTopic("test")
        } else {
            session.subscribe(toTopic: "test", at: .atMostOnce)
        }
        
    }
    @IBAction func publish(_ sender: Any) {
        self.session.publishData((self.publishText.text ?? "").data(using: String.Encoding.utf8, allowLossyConversion: false),
                                 onTopic: "test",
                                 retain: false,
                                 qos: .atMostOnce)
    }
    
    @IBAction func connectDisconnect(_ sender: Any) {
        switch self.session.status {
        case .connected:
            self.session.disconnect()
        case .closed, .created, .error:
            self.session.connect()
        default:
            return
        }
    }
}


