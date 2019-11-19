export function getCurrentUser() {
    return JSON.parse(localStorage.getItem('currentUser'))
}

export function convertDate(date) {
    let dateParts = date.split("T");
    if (dateParts.length !== 2) {
        return date;
    }
    return dateParts[0] + " " + dateParts[1].substr(0, 12)
}