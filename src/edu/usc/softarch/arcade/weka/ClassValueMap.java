package edu.usc.softarch.arcade.weka;

import java.util.HashMap;

/**
 * @author joshua
 *
 */
public class ClassValueMap {

	public static HashMap<String,String> LlamaChatMap = new HashMap<String,String>();
	public static HashMap<String,String> freecsMap = new HashMap<String,String>();

	ClassValueMap() {
		init();
	}
	
	public static void init() {
		LlamaChatMap.put("common.MessageQueue","d");
		LlamaChatMap.put("server.ClientConnection","c");
		LlamaChatMap.put("client.ChatPane","p");
		LlamaChatMap.put("client.LlamaChat","p");
		LlamaChatMap.put("common.sd.SD_UserDel","d");
		LlamaChatMap.put("client.ServerConnection","c");
		LlamaChatMap.put("client.LlamaChat$MyMouseListener","c");
		LlamaChatMap.put("common.sd.SD_Channel","d");
		LlamaChatMap.put("server.LlamaChatServer","p");
		LlamaChatMap.put("common.sd.SD_Error","d");
		LlamaChatMap.put("common.sd.SD_Chat","d");
		LlamaChatMap.put("server.ChannelManager","p");
		LlamaChatMap.put("common.sd.SD_UserAdd","d");
		LlamaChatMap.put("client.PrivateMsg$MsgWindow","p");
		LlamaChatMap.put("client.PrivateMsg","p");
		LlamaChatMap.put("common.sd.SD_AdminAdd","d");
		LlamaChatMap.put("common.sd.SD_Log","d");
		LlamaChatMap.put("common.sd.SD_Kick","d");
		LlamaChatMap.put("server.LlamaChatServer$ShutdownThread","p");
		LlamaChatMap.put("common.sd.SD_Rename","d");
		LlamaChatMap.put("server.LlamaChatServer$ChatFileItem","d");
		LlamaChatMap.put("common.sd.SD_Private","d");
		LlamaChatMap.put("common.sd.SD_Whisper","d");
		LlamaChatMap.put("client.PrivateMsg$MsgWindow$1","c");
		LlamaChatMap.put("client.PrivateMsg$MsgWindow$2","c");
		LlamaChatMap.put("client.LlamaChat$MyAction","c");
		LlamaChatMap.put("common.sd.SD_ServerCap","d");
		LlamaChatMap.put("client.LlamaChat$MyKeyListener","c");
		LlamaChatMap.put("server.ConfigParser","p");
		LlamaChatMap.put("client.CommandHistory","d");
		LlamaChatMap.put("server.ChannelManager$ChannelManagerItem","d");
		
		freecsMap.put("freecs.commands.AbstractCommand","p");
		freecsMap.put("freecs.commands.CommandSet","d");
		
		freecsMap.put("freecs.commands.CmdFriendsOnly","p");
		freecsMap.put("freecs.commands.CmdChangeBgcolor","p");
		freecsMap.put("freecs.commands.CmdThink","p");
		freecsMap.put("freecs.commands.CmdChangeColor","p");
		freecsMap.put("freecs.commands.CmdInvite","p");
		freecsMap.put("freecs.commands.CmdAccept","p");
		freecsMap.put("freecs.commands.CmdBan","p");
		freecsMap.put("freecs.commands.CmdRepeatedPrivateMessage","p");
		freecsMap.put("freecs.commands.CmdRespectUser","p");
		
		
		freecsMap.put("freecs.commands.CmdListOnlineFriends","p");
		freecsMap.put("freecs.commands.CmdHitDice","p");
		freecsMap.put("freecs.commands.CmdAct","p");
		freecsMap.put("freecs.commands.CmdListBan","p");
		freecsMap.put("freecs.commands.CmdShowIp","p");
		freecsMap.put("freecs.commands.CmdSetTheme","p");
		freecsMap.put("freecs.commands.CmdAddFriend","p");
		freecsMap.put("freecs.commands.CmdJoinClosed","p");
		freecsMap.put("freecs.commands.CmdCallMemberships","p");
		freecsMap.put("freecs.commands.CmdRSu","p");
		freecsMap.put("freecs.commands.CmdKickHard","p");
		freecsMap.put("freecs.commands.CmdKickToRoom","p");
		freecsMap.put("freecs.commands.CmdAck","p");
		freecsMap.put("freecs.commands.CmdJoin","p");
		freecsMap.put("freecs.commands.CmdPunish","p");
		freecsMap.put("freecs.commands.CmdSepa","p");
		freecsMap.put("freecs.commands.CmdSu","p");
		freecsMap.put("freecs.commands.CmdJoinUser","p");
		freecsMap.put("freecs.commands.CmdAway","p");
		freecsMap.put("freecs.commands.CmdKick","p");
		freecsMap.put("freecs.commands.CmdShout","p");
		freecsMap.put("freecs.commands.CmdReplyMessage","p");
		freecsMap.put("freecs.commands.CmdInviteAll","p");
		freecsMap.put("freecs.commands.CmdShowTime","p");
		freecsMap.put("freecs.commands.CmdQuit","p");
		freecsMap.put("freecs.commands.CmdLockChangeForeignAction","p");
		freecsMap.put("freecs.commands.CmdIgnore","p");
		freecsMap.put("freecs.commands.CmdUnBan","p");
		freecsMap.put("freecs.commands.CmdUnPunish","p");
		freecsMap.put("freecs.commands.CmdClear","p");
		freecsMap.put("freecs.commands.CmdShowUserDetail","p");
		freecsMap.put("freecs.commands.CmdUnlock","p");
		freecsMap.put("freecs.commands.CmdPrivateMessage","p");
		freecsMap.put("freecs.commands.CmdListUsers","p");
		freecsMap.put("freecs.commands.CmdQuestion","p");
		freecsMap.put("freecs.commands.CmdLock","p");
		freecsMap.put("freecs.commands.CmdMyCol","p");
		freecsMap.put("freecs.commands.CmdListAllFriends","p");
		freecsMap.put("freecs.commands.CmdSys","p");
		freecsMap.put("freecs.commands.CmdFun","p");
		freecsMap.put("freecs.commands.CmdRightChange","p");
		freecsMap.put("freecs.commands.CmdResetQuestioncounter","p");
		freecsMap.put("freecs.commands.CmdRemoveFriend","p");
		freecsMap.put("freecs.commands.CmdListVips","p");
		
		freecsMap.put("freecs.content.Connection","d");
		freecsMap.put("freecs.content.BanObject","d");
		
		freecsMap.put("freecs.core.Group","p");
		freecsMap.put("freecs.core.User","d");
		
		freecsMap.put("freecs.util.logger.LogWriter","p");
		
		freecsMap.put("freecs.Server","p");
		
		freecsMap.put("freecs.auth.AuthManager","p");
		freecsMap.put("freecs.util.FileMonitor","p");
		
		freecsMap.put("freecs.auth.sqlConnectionPool.SqlRunner","p");

		freecsMap.put("freecs.util.TrafficMonitor","p");
		freecsMap.put("freecs.util.ObjectBuffer","d");
		freecsMap.put("freecs.core.Membership","d");
		
		freecsMap.put("freecs.core.GroupManager","p");
		freecsMap.put("freecs.core.MessageRenderer","p");
		
		freecsMap.put("freecs.external.CmdConfirmHandler","p");
		freecsMap.put("freecs.layout.Template","p");
		
		freecsMap.put("freecs.external.WebadminRequestHandler","p");
		freecsMap.put("freecs.core.UserManager$UserStore$UserStoreIterator","d");
		
		freecsMap.put("freecs.util.EntityDecoder","p");
		
		freecsMap.put("freecs.util.HashUtils","p");
		
		freecsMap.put("freecs.layout.TemplateSet","d");
		freecsMap.put("freecs.content.ContentContainer","d");
		
		freecsMap.put("freecs.external.AbstractRequestHandler","p");
		freecsMap.put("freecs.external.StateRequestHandler","p");
		freecsMap.put("freecs.core.CentralSelector","c");
		freecsMap.put("freecs.core.Responder","c");
		freecsMap.put("freecs.core.Listener","c");
		freecsMap.put("freecs.core.RequestMonitor","p");
		
		freecsMap.put("freecs.core.CleanupClass","p");
		
		freecsMap.put("freecs.core.ScheduledAction","p");
		freecsMap.put("freecs.core.UserManager","p");
		
		freecsMap.put("freecs.core.User$1","d");
		
		freecsMap.put("freecs.auth.AbstractAuthenticator","p");
		freecsMap.put("freecs.auth.NoAuthentication","p");
		freecsMap.put("freecs.core.RequestEvaluator","c");
		freecsMap.put("freecs.layout.TemplateManager","p");
		freecsMap.put("freecs.content.HTTPRequest","d");
		
		freecsMap.put("freecs.core.UserManager$UserStore","d");
		freecsMap.put("freecs.external.UserlistRequestHandler","d");
		freecsMap.put("freecs.auth.sqlConnectionPool.ConnectionPool","d");
		freecsMap.put("freecs.auth.SQLAuthenticator","d");
		
		freecsMap.put("freecs.core.MessageParser","d");
		freecsMap.put("freecs.core.ConnectionBuffer","d");
		freecsMap.put("freecs.util.HttpDateParser","d");
		
		freecsMap.put("freecs.auth.sqlConnectionPool.DbProperties","p");
		
		freecsMap.put("freecs.core.RequestQueue","d");
		freecsMap.put("freecs.external.xmlrpc.XmlRpcManager","p");
		freecsMap.put("freecs.external.AdminCore","p");
		
		freecsMap.put("freecs.external.AccessForbiddenException","d");
		
		freecsMap.put("freecs.auth.sqlConnectionPool.PoolElement","p");
		
		freecsMap.put("freecs.core.RequestReader","p");
		freecsMap.put("freecs.content.PrivateMessageStore","d");
		
		freecsMap.put("freecs.core.MembershipManager","p");
		freecsMap.put("freecs.util.GroupUserList","d");
		freecsMap.put("freecs.util.CookieGenerator","p");
		
		freecsMap.put("freecs.core.CentralSelector$KeepAliveTimeoutChecker","p");
		freecsMap.put("freecs.util.logger.LogCleaner","p");
		freecsMap.put("freecs.util.FadeColor","p");
		freecsMap.put("freecs.content.MessageState","d");
		
		freecsMap.put("freecs.auth.XmlRpcAuthenticator","p");
		freecsMap.put("freecs.util.TrafficMonitor$AddressState","d");
		freecsMap.put("freecs.content.PersonalizedMessage","d");
		
		freecsMap.put("freecs.external.StaticRequestHandler","p");
		freecsMap.put("freecs.util.logger.LogFile$LogFileShrinker","p");
		freecsMap.put("freecs.core.CanceledRequestException","d");
		freecsMap.put("freecs.util.HttpAuth","p");
		freecsMap.put("freecs.util.logger.LogFile","p");
		freecsMap.put("freecs.content.ActionstoreObject","d");
		freecsMap.put("freecs.external.UserlistManager","p");
		freecsMap.put("freecs.external.StaticRequestHandler$FileProperties","d");
		freecsMap.put("freecs.util.HtmlEncoder","p");
		freecsMap.put("freecs.external.xmlrpc.freecsXmlRpcHandler","p");
		freecsMap.put("freecs.util.logger.LogWriterBenchmark","p");
		freecsMap.put("freecs.content.CallMembershipObject","d");
		freecsMap.put("freecs.util.logger.LogWriter$LogEntry","d");
	}
	
}
