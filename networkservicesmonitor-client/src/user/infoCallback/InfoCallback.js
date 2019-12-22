import React, {Component} from 'react';
import './InfoCallback.css';
import {Link} from 'react-router-dom';
import {Form} from 'antd';

const FormItem = Form.Item;

class InfoCallback extends Component {
    render() {
        return (
            <div className="info-callback-container">
                <div className="info-callback-container-box">
                    <h1>Potwierdzenie adresu e-mail</h1>
                    <div className="info-callback-content">
                        {this.props.match.params.status === 'true' ? (
                            <div>
                                <h3>Potwierdzono pomyślnie!</h3>

                                <p>Po aktywowaniu konta przez administratora uzyskasz dostęp do systemu.
                                    Nadanie dostępu zostanie potwierdzone wiadomością email.</p>
                            </div>
                        ) : (
                            <div>
                                <h3>Niepowodzenie</h3>
                                <p>Użyty link jest niepoprawny, wygasł lub został już wykorzystany.</p>
                            </div>
                        )}
                    </div>
                </div>
                <div className="info-callback-container-box-links">
                    <p><Link to="/login">Zaloguj</Link></p>
                </div>
            </div>
        );
    }
}


export default InfoCallback;