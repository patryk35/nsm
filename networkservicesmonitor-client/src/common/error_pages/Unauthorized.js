import React, {Component} from 'react';
import './NotFound.css';
import {Link} from 'react-router-dom';
import {Button} from 'antd';

class Unauthorized extends Component {
    render() {
        return (
            <div className="page-not-found">
                <h1 className="title">
                    401
                </h1>
                <div className="desc">
                    Nie posiadasz odpowiednich uprawnień, aby wyświetlić daną stronę.
                </div>
                <Link to="/"><Button className="go-back-btn" type="primary" size="large">Go Back</Button></Link>
            </div>
        );
    }
}

export default Unauthorized;