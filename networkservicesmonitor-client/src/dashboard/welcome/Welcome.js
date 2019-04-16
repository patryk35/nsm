import React, { Component } from 'react';
import './Welcome.css';
import logo from '../../logo.svg';
import {Row, Col} from 'antd';
import {Link} from "react-router-dom";


class Welcome extends Component {
    render() {
        return (
            <div className="welcome-container">
                <div className="welcome-content">
                <Row gutter={16} className="welcome-top-content">
                    <Col span={6}>
                        <img src={logo} alt="Logo" className="welcome-logo"/>
                    </Col>
                    <Col span={18}>
                        <h1 className="welcome-page-title">Network Services Monitor</h1>
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque vitae massa laoreet justo facilisis rhoncus vitae nec arcu. Suspendisse et ex mi. Sed lectus ipsum, accumsan at maximus bibendum, ornare ut neque. Donec pellentesque ipsum non lorem lobortis bibendum. Vestibulum tempus accumsan orci, blandit tincidunt nulla. Maecenas nunc leo, hendrerit in nisl in, lobortis consequat nisi. Donec pharetra libero sed finibus accumsan. Curabitur blandit, purus et interdum elementum, tellus erat rutrum orci, vel imperdiet velit nisl sit amet magna. Praesent tincidunt non risus vel mollis. Praesent iaculis volutpat ipsum. Nullam vitae quam sit amet ligula vulputate suscipit et in nunc. Morbi scelerisque, nisl ac accumsan hendrerit, massa tortor vehicula leo, non interdum lacus sem vitae elit.

                        Aenean congue gravida massa. Cras dapibus convallis feugiat. Proin luctus egestas enim, ac cursus nisi faucibus in. Nam a molestie metus, ac vestibulum erat. Donec convallis vestibulum mi vitae molestie. Donec id blandit massa. Vestibulum dui massa, ornare eget ex id, accumsan ultricies metus. Praesent aliquam nisi in egestas elementum. Curabitur aliquet mi non lectus placerat vestibulum. Mauris pellentesque sem sed varius pellentesque. Nulla sed nibh id ligula fermentum tristique. Duis facilisis, ante et posuere tincidunt, enim quam feugiat nibh, ac fermentum eros diam ac lacus.
                    </Col>
                </Row>

                    Maecenas pulvinar sed metus ut laoreet. Suspendisse vitae imperdiet ipsum. Suspendisse rhoncus egestas venenatis. Donec dapibus et urna ut imperdiet. Aliquam ullamcorper nisl nulla, sit amet posuere leo facilisis a. Morbi et purus eleifend, commodo mi eget, egestas lacus. Integer pharetra, lorem sit amet imperdiet imperdiet, justo felis convallis lectus, vel posuere augue tortor id nunc.

                    Duis eu gravida turpis, et cursus ex. Sed fermentum lacus quis convallis bibendum. Duis sed nunc neque. In venenatis accumsan molestie. Vestibulum a egestas nibh. Pellentesque dapibus pretium lectus quis varius. Nam vitae accumsan tortor. In id ullamcorper sem. Maecenas ut nibh bibendum, iaculis turpis eget, mollis magna. Vestibulum et vehicula ex, ac fringilla dui. Ut a pulvinar mauris, sed molestie lectus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Fusce convallis, odio a viverra facilisis, eros turpis mattis eros, ac porta mauris ipsum et ex. Sed sodales, justo vel tempor facilisis, nunc massa tempor neque, id posuere ipsum leo eget eros. Nullam ultrices sed ipsum eu tincidunt.

                    Pellentesque libero urna, pretium ut purus eget, finibus dapibus ipsum. Integer sagittis rhoncus orci sit amet interdum. Pellentesque in nisl dui. Sed tincidunt sollicitudin dui luctus tristique. Praesent fermentum efficitur odio, et laoreet diam consequat vitae. Donec nibh ante, tristique aliquam ligula ac, tincidunt sagittis purus. Donec eu feugiat purus. Quisque convallis tortor quis metus dapibus congue. Mauris rutrum volutpat ultricies. Duis pulvinar elit nunc, in euismod urna porta ac. In nec urna tortor.

                </div>
            </div>
        );
    }
}




export default Welcome;