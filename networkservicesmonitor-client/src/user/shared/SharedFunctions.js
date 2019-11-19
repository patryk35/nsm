import {EMAIL_MAX_LENGTH, PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH} from "../../configuration";

const EMAIL_REGEX = RegExp('^(([^<>()\\[\\]\\\\.,;:\\s@"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@"]+)*)|(".+"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$');

export const validateEmail = (email, validateStatus = null, message = null) => {
    if (!email) {
        validateStatus = 'error';
        message = 'Pole nie może być puste';
    } else if (!EMAIL_REGEX.test(email)) {
        validateStatus = 'error';
        message = 'Podano adres jest nieprawidłowy';
    } else if (email.length > EMAIL_MAX_LENGTH) {
        validateStatus = 'error';
        message = `Podany adres jest zbyt długi (email nie może być dłuższy niż ${EMAIL_MAX_LENGTH} znaków)`;
    }

    return {
        validateStatus: validateStatus,
        message: message
    }
};

export const validateEmailOnce = (email) => {
    return validateEmail(email, 'success', null);
};

export const validatePassword = (password) => {
    let validateStatus = 'success';
    let message = null;
    if (password.length < PASSWORD_MIN_LENGTH || password.length > PASSWORD_MAX_LENGTH) {
        validateStatus = 'error';
        message = `Hasło powinno mieć między ${PASSWORD_MIN_LENGTH} a ${PASSWORD_MAX_LENGTH} znaków`;
    }

    return {
        validateStatus: validateStatus,
        message: message
    };

};