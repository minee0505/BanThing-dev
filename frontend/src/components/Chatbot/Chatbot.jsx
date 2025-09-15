import React, { useState, useEffect, useRef } from 'react';
import { sendMessageToChatbot, getChatbotHistory, isUserAuthenticated } from '../../services/chatbotApi';
import './Chatbot.scss';

const Chatbot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const messagesEndRef = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => {
        const authStatus = isUserAuthenticated();
        setIsAuthenticated(authStatus);
        if (authStatus) {
            loadChatHistory();
        }
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        if (isOpen && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isOpen]);

    const loadChatHistory = async () => {
        try {
            const result = await getChatbotHistory();
            if (result.success) {
                const history = result.data.slice(-10);
                const formattedMessages = [];

                history.forEach(item => {
                    formattedMessages.push({
                        type: 'user',
                        content: item.userMessage,
                        timestamp: new Date(item.createdAt)
                    });
                    formattedMessages.push({
                        type: 'bot',
                        content: item.botResponse,
                        timestamp: new Date(item.createdAt)
                    });
                });

                setMessages(formattedMessages);
            }
        } catch (error) {
            console.error('대화 기록 로드 실패:', error);
        }
    };

    const sendMessage = async () => {
        if (!inputMessage.trim() || isLoading) return;

        const userMessage = inputMessage.trim();
        setInputMessage('');
        setIsLoading(true);

        const newUserMessage = {
            type: 'user',
            content: userMessage,
            timestamp: new Date()
        };
        setMessages(prev => [...prev, newUserMessage]);

        try {
            const result = await sendMessageToChatbot(userMessage);

            if (result.success) {
                const botMessage = {
                    type: 'bot',
                    content: result.data.response,
                    timestamp: new Date()
                };
                setMessages(prev => [...prev, botMessage]);
            } else {
                throw new Error(result.error || '응답 처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('메시지 전송 실패:', error);

            const errorMessage = {
                type: 'bot',
                content: '죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
                timestamp: new Date(),
                isError: true
            };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const toggleChatbot = () => {
        if (!isAuthenticated) {
            alert('로그인 후 이용해주세요.');
            return;
        }
        setIsOpen(!isOpen);
    };

    const formatMessage = (content) => {
        return content.split('\n').map((line, index) => (
            <React.Fragment key={index}>
                {line}
                {index < content.split('\n').length - 1 && <br />}
            </React.Fragment>
        ));
    };

    if (!isAuthenticated) {
        return (
            <div className="chatbot-container">
                <button
                    className="chatbot-trigger chatbot-trigger--disabled"
                    onClick={toggleChatbot}
                    aria-label="AI 도우미 (로그인 필요)"
                >
                    <span className="chatbot-icon">🤖</span>
                </button>
            </div>
        );
    }

    return (
        <div className="chatbot-container">
            <button
                className={`chatbot-trigger ${isOpen ? 'chatbot-trigger--active' : ''}`}
                onClick={toggleChatbot}
                aria-label="AI 도우미"
            >
        <span className="chatbot-icon">
          {isOpen ? '✕' : '🤖'}
        </span>
            </button>

            {isOpen && (
                <div className="chatbot-window">
                    <div className="chatbot-header">
                        <div className="chatbot-header__title">
                            <span className="chatbot-header__icon">🤖</span>
                            <span className="chatbot-header__text">반띵 AI 도우미</span>
                        </div>
                        <button
                            className="chatbot-header__close"
                            onClick={toggleChatbot}
                            aria-label="닫기"
                        >
                            ✕
                        </button>
                    </div>

                    <div className="chatbot-messages">
                        {messages.length === 0 ? (
                            <div className="chatbot-welcome">
                                <div className="chatbot-welcome__icon">👋</div>
                                <div className="chatbot-welcome__text">
                                    안녕하세요! 반띵 AI 도우미입니다.<br />
                                    소분 모임 찾기, 이용 방법 등을<br />
                                    도와드릴 수 있어요!
                                </div>
                            </div>
                        ) : (
                            messages.map((message, index) => (
                                <div
                                    key={index}
                                    className={`chatbot-message chatbot-message--${message.type} ${message.isError ? 'chatbot-message--error' : ''}`}
                                >
                                    <div className="chatbot-message__content">
                                        {formatMessage(message.content)}
                                    </div>
                                    <div className="chatbot-message__time">
                                        {message.timestamp.toLocaleTimeString([], {
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </div>
                                </div>
                            ))
                        )}

                        {isLoading && (
                            <div className="chatbot-message chatbot-message--bot">
                                <div className="chatbot-message__content">
                                    <div className="chatbot-typing">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                        )}

                        <div ref={messagesEndRef} />
                    </div>

                    <div className="chatbot-input">
                        <div className="chatbot-input__wrapper">
              <textarea
                  ref={inputRef}
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="메시지를 입력하세요..."
                  className="chatbot-input__field"
                  rows="1"
                  disabled={isLoading}
              />
                            <button
                                onClick={sendMessage}
                                disabled={!inputMessage.trim() || isLoading}
                                className="chatbot-input__send"
                                aria-label="전송"
                            >
                <span className="chatbot-input__send-icon">
                  {isLoading ? '⏳' : '📤'}
                </span>
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Chatbot;