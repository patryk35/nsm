import React from 'react';
import {Redirect, Route} from "react-router-dom";


const PrivateRoute = ({component: Component, user, role, authenticated, ...rest}) => (
    <Route
        {...rest}
        render={props =>
            authenticated ? (
                (user.roles.includes(role) || user.roles.includes("ROLE_ADMINISTRATOR")) ? (
                    <Component {...rest} {...props} />
                ) : (
                    <Redirect
                        to={{
                            pathname: '/401',
                            state: {from: props.location}
                        }}
                    />
                )
            ) : (
                <Redirect
                    to={{
                        pathname: '/login',
                        state: {from: props.location}
                    }}
                />
            )
        }
    />
);

export default PrivateRoute